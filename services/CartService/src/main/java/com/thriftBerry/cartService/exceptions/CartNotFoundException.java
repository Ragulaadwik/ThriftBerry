package com.thriftBerry.cartService.exceptions;

/**
 * Exception thrown when a cart is not found for a given user ID.
 * This is a checked exception that should be handled explicitly by the caller.
 */
public class CartNotFoundException extends RuntimeException {
    
    private final Long userId;

    public CartNotFoundException(Long userId) {
        super(String.format("Cart not found for user ID: %d", userId));
        this.userId = userId;
    }

    public CartNotFoundException(String message) {
        super(message);
        this.userId = null;
    }

    public Long getUserId() {
        return userId;
    }
}

