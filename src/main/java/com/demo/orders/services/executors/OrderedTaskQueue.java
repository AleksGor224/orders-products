package com.demo.orders.services.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderedTaskQueue {
    private final String orderId;
    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    public OrderedTaskQueue(String orderId) {
        this.orderId = orderId;
    }

    public void submitTask(Runnable task) {
        tasks.add(task);
    }

    public Runnable takeTask() throws InterruptedException {
        return tasks.take();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public String getOrderId() {
        return orderId;
    }
}