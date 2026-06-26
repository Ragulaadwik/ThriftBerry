package com.thriftBerry.orderService.exception;

public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String invalidOrderId) {
        super("Invalid order ID: " + invalidOrderId);
    }
}
