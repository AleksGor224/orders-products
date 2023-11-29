package com.demo.orders.services;

import com.demo.orders.CommonTestUtils;
import com.demo.orders.dtos.ProductDto;
import com.demo.orders.dtos.ProductViewDto;
import com.demo.orders.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductsServiceTest extends IntegrationTestsBase {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.3");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void testCreateProduct() {
        ProductDto productDto = new ProductDto(UUID.randomUUID().toString(), "Test Product", 29.99);
        String productId = productsService.createProduct(productDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.existsById(productId), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);
        ProductViewDto createdProduct = productsService.getProductById(productId);

        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getProductName()).isEqualTo("Test Product");
        assertThat(createdProduct.getProductPrice()).isEqualTo(29.99);
    }

    @Test
    void testGetProductById() {
        ProductDto productDto = new ProductDto(UUID.randomUUID().toString(), "Test Product", 29.99);
        String productId = productsService.createProduct(productDto);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.existsById(productId), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);
        ProductViewDto foundProduct = productsService.getProductById(productId);

        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getProductId()).isEqualTo(productId);
    }

    @Test
    void testGetProductByIdNotFound() {
        assertThrows(ProductNotFoundException.class, () -> {
            productsService.getProductById(UUID.randomUUID().toString());
        });
    }

    @Test
    void testUpdateProduct() {
        ProductDto productDto = new ProductDto(UUID.randomUUID().toString(), "Test Product", 29.99);
        String productId = productsService.createProduct(productDto);

        String finalProductId1 = productId;
        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.existsById(finalProductId1), Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);
        ProductDto updateDto = new ProductDto(productId, "Updated Product", 39.99);

        long currTime = System.currentTimeMillis();
        productId = productsService.updateProduct(productId, updateDto);

        String finalProductId = productId;
        status = CommonTestUtils.waitForResultWithTimeout(() -> productsRepository.findById(finalProductId).get().getUpdateDate() >= currTime, Duration.ofSeconds(2), Duration.ofMillis(100));
        Assertions.assertTrue(status);
        ProductViewDto updatedProduct = productsService.getProductById(productId);

        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getProductName()).isEqualTo("Updated Product");
        assertThat(updatedProduct.getProductPrice()).isEqualTo(39.99);
    }

    @Test
    void testDeleteProduct() {
        ProductDto productDto = new ProductDto(UUID.randomUUID().toString(), "Test Product", 29.99);
        String productId = productsService.createProduct(productDto);

        productsService.deleteProduct(productId);

        boolean status = CommonTestUtils.waitForResultWithTimeout(() -> !productsRepository.existsById(productId), Duration.ofSeconds(10), Duration.ofMillis(100));
        Assertions.assertTrue(status);
    }

    @AfterEach
    void cleanUp() {
        productsRepository.deleteAll();
    }
}