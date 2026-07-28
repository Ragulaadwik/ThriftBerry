package com.thriftBerry.orderService.exception;

import com.thriftBerry.orderService.dto.ExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderServiceGlobalExceptionHandler {

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidUserException(InvalidUserException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidOrderException(InvalidOrderException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidOrderStateException(InvalidOrderStateException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleOrderNotFoundException(OrderNotFoundException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleOrderProcessingException(OrderProcessingException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(500).body(response);
    }



}
