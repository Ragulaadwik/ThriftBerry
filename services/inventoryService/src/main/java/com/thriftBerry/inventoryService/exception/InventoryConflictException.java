package com.thriftBerry.inventoryService.exception;

public class InventoryConflictException extends RuntimeException{
    public InventoryConflictException(String message) {
        super(message);
    }
}
