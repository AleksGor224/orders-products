package com.demo.orders.services;

import com.demo.orders.CommonUtils;
import com.demo.orders.dtos.ProductDto;
import com.demo.orders.dtos.ProductViewDto;
import com.demo.orders.exceptions.ProductNotFoundException;
import com.demo.orders.repo.LinksOrdersProductsRepository;
import com.demo.orders.repo.ProductsRepository;
import com.demo.orders.repo.entities.Product;
import com.demo.orders.services.executors.OrderedTaskManager;
import com.demo.orders.services.executors.TaskManager;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final TaskManager taskManager;
    private final OrderedTaskManager orderedTaskManager;
    private final TransactionalInvoker transactionalInvoker;
    private final LinksOrdersProductsRepository linksOrdersProductsRepository;

    @Autowired
    public ProductsService(ProductsRepository productsRepository,
                           LinksOrdersProductsRepository linksOrdersProductsRepository,
                           TransactionalInvoker transactionalInvoker,
                           @Value("${nonConcurrentTaskThreadsNumber}") int nonConTaskThreadsNum,
                           @Value("${concurrentTaskThreadsNumber}") int conTaskThreadsNum) {
        this.productsRepository = productsRepository;
        this.transactionalInvoker = transactionalInvoker;
        this.linksOrdersProductsRepository = linksOrdersProductsRepository;
        this.taskManager = new TaskManager(nonConTaskThreadsNum);
        this.orderedTaskManager = new OrderedTaskManager(conTaskThreadsNum);
    }

    @PreDestroy
    public void cleanUp() {
        taskManager.shutdown();
        orderedTaskManager.shutdown();
    }

    public String createProduct(ProductDto productDto) {
        String generatedProductId = UUID.randomUUID().toString();
        taskManager.submitCreateTask(() -> transactionalInvoker.invokeTransactional(() -> {
            Product product = mapDtoToEntity(productDto, generatedProductId);
            productsRepository.save(product);
        }));
        return generatedProductId;
    }

    public ProductViewDto getProductById(String id) {
        return mapEntityToViewDto(getProductEntityById(id));
    }

    private Product getProductEntityById(String id) {
        return productsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));
    }

    public List<ProductViewDto> getProductsIn(List<String> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Product> products = productsRepository.findByProductIdIn(ids);
        return products.stream().map(ProductsService::mapEntityToViewDto).toList();
    }

    public String updateProduct(String id, ProductDto productDto) {

        Product product = getProductEntityById(id);

        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> {
            Product p = getProductEntityById(id);
            if (productDto.getProductName() != null) {
                if (productDto.getProductName().isEmpty()) {
                    p.setProductName(null);
                } else {
                    p.setProductName(productDto.getProductName());
                }
            }
            if (productDto.getProductPrice() != null) {
                p.setProductPrice(productDto.getProductPrice());
            }
            p.setUpdateDate(System.currentTimeMillis());
            productsRepository.save(p);
        });

        orderedTaskManager.submitTask(product.getProductId(), r);

        return product.getProductId();
    }

    public void deleteProduct(String id) {
        orderedTaskManager.submitTask(id,
                () -> transactionalInvoker.invokeTransactional(() -> {
                    productsRepository.deleteById(id);
                    linksOrdersProductsRepository.deleteByProductId(id);
                }));
    }

    private static Product mapDtoToEntity(ProductDto dto, String id) {
        long currTime = System.currentTimeMillis();
        return new Product(id, dto.getProductName(), dto.getProductPrice(), currTime, currTime);
    }

    private static ProductViewDto mapEntityToViewDto(Product from) {
        return new ProductViewDto(from.getProductId(), from.getProductName(), from.getProductPrice(), CommonUtils.mapEpochMillisToTimestamp(from.getCreateDate()), CommonUtils.mapEpochMillisToTimestamp(from.getUpdateDate()));
    }
}