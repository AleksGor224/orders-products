package com.demo.orders.services;

import com.demo.orders.CommonUtils;
import com.demo.orders.dtos.OrderDto;
import com.demo.orders.dtos.OrderViewDto;
import com.demo.orders.dtos.ProductViewDto;
import com.demo.orders.exceptions.LinkOrderProductsNotFoundException;
import com.demo.orders.exceptions.OrderNotFoundException;
import com.demo.orders.repo.LinksOrdersProductsRepository;
import com.demo.orders.repo.OrdersRepository;
import com.demo.orders.repo.entities.LinkOrderProducts;
import com.demo.orders.repo.entities.Order;
import com.demo.orders.services.executors.OrderedTaskManager;
import com.demo.orders.services.executors.TaskManager;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final TaskManager taskManager;
    private final OrderedTaskManager orderedTaskManager;
    private final OrdersRepository ordersRepository;
    private final LinksOrdersProductsRepository linksOrdersProductsRepository;
    private final ProductsService productsService;
    private final TransactionalInvoker transactionalInvoker;

    @Autowired
    public OrderService(OrdersRepository ordersRepository,
                        ProductsService productsService,
                        LinksOrdersProductsRepository linksOrdersProductsRepository,
                        TransactionalInvoker transactionalInvoker,
                        @Value("${nonConcurrentTaskThreadsNumber}") int nonConTaskThreadsNum,
                        @Value("${concurrentTaskThreadsNumber}") int conTaskThreadsNum) {
        this.ordersRepository = ordersRepository;
        this.productsService = productsService;
        this.linksOrdersProductsRepository = linksOrdersProductsRepository;
        this.transactionalInvoker = transactionalInvoker;
        this.taskManager = new TaskManager(nonConTaskThreadsNum);
        this.orderedTaskManager = new OrderedTaskManager(conTaskThreadsNum);
    }

    @PreDestroy
    public void cleanUp() {
        taskManager.shutdown();
        orderedTaskManager.shutdown();
    }

    public String createOrder(OrderDto orderDto) {
        String generatedId = UUID.randomUUID().toString();
        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> {
            Order order = mapDtoToEntity(orderDto, generatedId);
            ordersRepository.save(order);
        });
        taskManager.submitCreateTask(r);
        return generatedId;
    }

    public OrderViewDto getOrderById(String id) {
        Order order = getOrderEntityById(id);
        return getOrderViewDto(order);
    }

    public List<OrderViewDto> getAllOrders() {
        return ordersRepository.findAll().stream().map(this::getOrderViewDto).toList();
    }

    public String updateOrder(String id, OrderDto orderDto) {
        Order order = getOrderEntityById(id);
        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> {
            order.setOrderName(StringUtils.hasText(orderDto.getOrderName()) ? orderDto.getOrderName() : null);
            order.setUpdateDate(System.currentTimeMillis());
            ordersRepository.save(order);
        });
        orderedTaskManager.submitTask(order.getOrderId(), r);
        return order.getOrderId();
    }

    public void deleteOrder(String id) {
        Order order = getOrderEntityById(id);
        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> ordersRepository.deleteById(id));
        orderedTaskManager.submitTask(order.getOrderId(), r);
    }

    public void addProductToOrder(String orderId, String productId) {
        Order order = getOrderEntityById(orderId);
        productsService.getProductById(productId);
        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> {
            LinkOrderProducts link = new LinkOrderProducts();
            link.setOrderId(order.getOrderId());
            link.setProductId(productId);
            linksOrdersProductsRepository.save(link);
        });
        orderedTaskManager.submitTask(order.getOrderId(), r);
    }

    public void removeProductFromOrder(String orderId, String productId) {
        Order order = getOrderEntityById(orderId);
        productsService.getProductById(productId);
        Runnable r = () -> transactionalInvoker.invokeTransactional(() -> {
            LinkOrderProducts link = linksOrdersProductsRepository.findByOrderIdAndProductId(orderId, productId).orElseThrow(() -> new LinkOrderProductsNotFoundException(String.format("A link for the order with id '%s' was not found", orderId)));
            linksOrdersProductsRepository.delete(link);
            productsService.deleteProduct(productId);
        });
        orderedTaskManager.submitTask(order.getOrderId(), r);
    }

    private OrderViewDto getOrderViewDto(Order order) {
        OrderViewDto orderViewDto = mapEntityToViewWithEmptyProducts(order);
        List<String> productIds = linksOrdersProductsRepository.findByOrderId(order.getOrderId()).stream()
                .map(LinkOrderProducts::getProductId).toList();
        if (!productIds.isEmpty()) {
            List<ProductViewDto> productViewDtoList = productsService.getProductsIn(productIds);
            orderViewDto.setProducts(productViewDtoList);
        }
        return orderViewDto;
    }

    private Order getOrderEntityById(String id) {
        return ordersRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));
    }

    private static Order mapDtoToEntity(OrderDto from, String generatedId) {
        long currTime = System.currentTimeMillis();
        return new Order(generatedId, from.getOrderName(), currTime, currTime);
    }

    private static OrderViewDto mapEntityToViewWithEmptyProducts(Order from) {
        return new OrderViewDto(from.getOrderId(), from.getOrderName(), new ArrayList<>(), CommonUtils.mapEpochMillisToTimestamp(from.getCreateDate()), CommonUtils.mapEpochMillisToTimestamp(from.getUpdateDate()));
    }
}