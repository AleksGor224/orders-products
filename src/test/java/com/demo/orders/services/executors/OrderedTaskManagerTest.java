package com.demo.orders.services.executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class OrderedTaskManagerTest {
    private OrderedTaskManager orderedTaskManager;

    @BeforeEach
    public void setUp() {
        orderedTaskManager = new OrderedTaskManager(4);
    }

    @AfterEach
    public void tearDown() {
        orderedTaskManager.shutdown();
    }

    @Test
    void testConcurrentExecution() throws InterruptedException {
        int numberOfTasks = 100;
        AtomicInteger count = new AtomicInteger();
        String orderId = UUID.randomUUID().toString();
        CountDownLatch latch = new CountDownLatch(numberOfTasks);

        for (int i = 0; i < numberOfTasks; i++) {
            orderedTaskManager.submitTask(orderId, () -> {
                try {
                    count.getAndIncrement();
                } finally {
                    latch.countDown();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        latch.await();
        Assertions.assertEquals(100, count.get());
    }
}