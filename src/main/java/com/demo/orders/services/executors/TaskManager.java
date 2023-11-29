package com.demo.orders.services.executors;

public class TaskManager {
    private final TaskExecutor createExecutor;

    public TaskManager(int threadNum) {
        createExecutor = new TaskExecutor(threadNum);
    }

    public void submitCreateTask(Runnable task) {
        createExecutor.submitTask(task);
    }

    public void shutdown() {
        createExecutor.shutdown();
    }
}