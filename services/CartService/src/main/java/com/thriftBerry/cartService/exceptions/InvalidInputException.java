package com.thriftBerry.cartService.exceptions;

/**
 * Exception thrown when input validation fails.
 * Used for invalid user IDs, quantities, or other input parameters.
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}

