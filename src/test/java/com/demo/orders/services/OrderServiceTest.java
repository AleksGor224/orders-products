package com.demo.orders.services;

import com.demo.orders.CommonTestUtils;
import com.demo.orders.dtos.OrderDto;
import com.demo.orders.dtos.OrderViewDto;
import com.demo.orders.dtos.ProductDto;
import com.demo.orders.exceptions.OrderNotFoundException;
import com.demo.orders.repo.entities.LinkOrderProducts;
import com.demo.orders.repo.entities.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest extends IntegrationTestsBase {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.3");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void createOrder_ShouldCreateOrder() {
        OrderDto orderDto = new OrderDto("New Order");

        String id = orderService.createOrder(orderDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.existsById(id), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);
        OrderViewDto createdOrder = orderService.getOrderById(id);

        assertNotNull(createdOrder);
        assertEquals(orderDto.getOrderName(), createdOrder.getOrderName());
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {

        OrderDto orderDto = new OrderDto("Existing Order");

        String id = orderService.createOrder(orderDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.existsById(id), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        OrderViewDto foundOrder = orderService.getOrderById(id);

        assertNotNull(foundOrder);
        assertEquals(orderDto.getOrderName(), foundOrder.getOrderName());
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldThrowException() {
        String nonExistentOrderId = UUID.randomUUID().toString();
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(nonExistentOrderId));
    }

    @Test
    void updateOrder_WhenOrderExists_ShouldUpdateOrder() {
        long createTime = System.currentTimeMillis();
        Order existingOrder = new Order(UUID.randomUUID().toString(), "Original Order", createTime, createTime);
        ordersRepository.save(existingOrder);
        OrderDto updateDto = new OrderDto("Updated Order");

        String id = orderService.updateOrder(existingOrder.getOrderId(), updateDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.findById(existingOrder.getOrderId()).get().getUpdateDate() > createTime, Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        OrderViewDto updatedOrder = orderService.getOrderById(id);

        assertNotNull(updatedOrder);
        assertEquals(updateDto.getOrderName(), updatedOrder.getOrderName());
    }

    @Test
    void updateOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        String nonExistentOrderId = UUID.randomUUID().toString();
        OrderDto updateDto = new OrderDto("Updated Order");

        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder(nonExistentOrderId, updateDto));
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteOrder() {
        Order existingOrder = new Order(UUID.randomUUID().toString(), "Order to delete", System.currentTimeMillis(), System.currentTimeMillis());
        ordersRepository.save(existingOrder);

        orderService.deleteOrder(existingOrder.getOrderId());
        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> !ordersRepository.existsById(existingOrder.getOrderId()), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        assertFalse(ordersRepository.existsById(existingOrder.getOrderId()));
    }

    @Test
    void deleteOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        String nonExistentOrderId = UUID.randomUUID().toString();

        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(nonExistentOrderId));
    }

    @Test
    void addProductToOrder_WhenOrderAndProductExist_ShouldAddProduct() {
        Order existingOrder = new Order(UUID.randomUUID().toString(), "Order", System.currentTimeMillis(), System.currentTimeMillis());
        ordersRepository.save(existingOrder);
        ProductDto productToAdd = new ProductDto(existingOrder.getOrderId(), "Product", 100.0);
        String productId = productsService.createProduct(productToAdd);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.existsById(productId), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        long currTime = System.currentTimeMillis();
        orderService.addProductToOrder(existingOrder.getOrderId(), productId);

        CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.findById(existingOrder.getOrderId()).get().getUpdateDate() >= currTime, Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        OrderViewDto updatedOrder = orderService.getOrderById(existingOrder.getOrderId());
        assertNotNull(updatedOrder);
        assertEquals(productToAdd.getProductName(), updatedOrder.getProducts().get(0).getProductName());
        assertEquals(productToAdd.getProductPrice(), updatedOrder.getProducts().get(0).getProductPrice());
        assertFalse(updatedOrder.getProducts().get(0).getCreateDate().isEmpty());
        assertFalse(updatedOrder.getProducts().get(0).getUpdateDate().isEmpty());
    }

    @Test
    void addProductToOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        String nonExistentOrderId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        assertThrows(OrderNotFoundException.class, () -> orderService.addProductToOrder(nonExistentOrderId, productId));
    }

    @Test
    void removeProductFromOrder_WhenOrderAndProductExist_ShouldRemoveProduct() {
        Order existingOrder = new Order(UUID.randomUUID().toString(), "Order", System.currentTimeMillis(), System.currentTimeMillis());
        ordersRepository.save(existingOrder);
        String productId = productsService.createProduct(new ProductDto(existingOrder.getOrderId(), "Product", 100.0));

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.existsById(productId), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        LinkOrderProducts linkOrderProducts = new LinkOrderProducts();
        linkOrderProducts.setProductId(productId);
        linkOrderProducts.setOrderId(existingOrder.getOrderId());
        linksOrdersProductsRepository.save(linkOrderProducts);

        orderService.removeProductFromOrder(existingOrder.getOrderId(), productId);

        CommonTestUtils.waitForResultWithTimeout(() -> !productsRepository.existsById(productId), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        OrderViewDto updatedOrder = orderService.getOrderById(existingOrder.getOrderId());
        assertNotNull(updatedOrder);
        assertFalse(updatedOrder.getProducts().stream().anyMatch(p -> p.getProductId().equals(productId)));
    }

    @Test
    void removeProductFromOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        String nonExistentOrderId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        assertThrows(OrderNotFoundException.class, () -> orderService.removeProductFromOrder(nonExistentOrderId, productId));
    }

    @Test
    void getAllOrders() {
        OrderDto orderDto = new OrderDto("Order1");

        String id = orderService.createOrder(orderDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.existsById(id), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        orderDto = new OrderDto("Order2");

        String id2 = orderService.createOrder(orderDto);

        status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.existsById(id2), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        orderDto = new OrderDto("Order3");

        String id3 = orderService.createOrder(orderDto);

        status = CommonTestUtils.waitForResultWithTimeout(() -> ordersRepository.existsById(id3), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);

        List<OrderViewDto> allOrders = orderService.getAllOrders();

        Assertions.assertEquals(3, allOrders.size());

        Set<String> ids = new HashSet<>();

        ids.add(id);
        ids.add(id2);
        ids.add(id3);

        for (int i = 0; i < 3; i++) {
            assertTrue(ids.contains(allOrders.get(i).getOrderId()));
            ids.remove(allOrders.get(i).getOrderId());
            Assertions.assertEquals(allOrders.get(i).getOrderName().replace("Order", ""), String.valueOf(i + 1));
        }

        assertTrue(ids.isEmpty());
    }
}