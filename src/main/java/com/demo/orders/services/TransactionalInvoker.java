package com.demo.orders.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalInvoker {

    @Transactional
    public void invokeTransactional(Runnable runnable) {
        runnable.run();
    }
}
