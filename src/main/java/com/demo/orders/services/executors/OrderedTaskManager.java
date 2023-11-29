package com.demo.orders.services.executors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderedTaskManager {
    private final ConcurrentMap<String, OrderedTaskQueue> orderQueues = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public OrderedTaskManager(int numberOfThreads) {
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void submitTask(String orderId, Runnable task) {
        OrderedTaskQueue queue = orderQueues.computeIfAbsent(orderId, OrderedTaskQueue::new);
        queue.submitTask(task);
        processTasks(queue);
    }

    private void processTasks(OrderedTaskQueue queue) {
        executorService.submit(() -> {
            try {
                Runnable task = queue.takeTask();
                task.run();
                if (queue.isEmpty()) {
                    orderQueues.remove(queue.getOrderId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}