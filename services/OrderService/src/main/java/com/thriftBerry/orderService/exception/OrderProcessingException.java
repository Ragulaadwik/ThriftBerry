package com.thriftBerry.orderService.exception;

public class OrderProcessingException extends RuntimeException {
    public OrderProcessingException(String s) {
        super(s);
    }
}
