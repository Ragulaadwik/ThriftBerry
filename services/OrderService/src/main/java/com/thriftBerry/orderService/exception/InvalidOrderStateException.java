package com.thriftBerry.orderService.exception;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String s) {
        super(s);
    }
}
