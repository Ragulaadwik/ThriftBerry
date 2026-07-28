package com.thriftBerry.cartService.exceptions;

public class CartFetchException extends RuntimeException {
    public CartFetchException(String message) {
        super(message);
    }

    public CartFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
