package com.thriftBerry.orderService.exception;

public class OrderNotFoundException extends BaseException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}