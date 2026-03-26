package com.thriftBerry.inventoryService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryGlobalException {

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<String> inventoryNotFoundHandler(InventoryNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(InventoryConflictException.class)
    public ResponseEntity<String> inventoryConflictHandler(InventoryConflictException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

}
