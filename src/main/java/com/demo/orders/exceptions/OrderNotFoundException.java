package com.demo.orders.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String s) {
        super(s);
    }
}
