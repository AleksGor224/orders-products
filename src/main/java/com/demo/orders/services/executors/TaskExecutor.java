package com.demo.orders.services.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskExecutor {
    private final BlockingQueue<Runnable> taskQueue;
    private final ExecutorService executorService;

    public TaskExecutor(int numberOfThreads) {
        taskQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable task = taskQueue.take();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    public void submitTask(Runnable task) {
        taskQueue.add(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}