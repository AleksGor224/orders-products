package com.demo.orders.services;

import com.demo.orders.repo.LinksOrdersProductsRepository;
import com.demo.orders.repo.OrdersRepository;
import com.demo.orders.repo.ProductsRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataMongoTest
@Import({OrderService.class, ProductsService.class, TransactionalInvoker.class})
public abstract class IntegrationTestsBase {

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    ProductsRepository productsRepository;

    @Autowired
    LinksOrdersProductsRepository linksOrdersProductsRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    ProductsService productsService;

    @AfterEach
    void tearDown() {
        ordersRepository.deleteAll();
        productsRepository.deleteAll();
        linksOrdersProductsRepository.deleteAll();
    }
}
