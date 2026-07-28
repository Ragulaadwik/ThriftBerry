package com.thriftBerry.ProductService.exception;

import com.thriftBerry.ProductService.dto.ExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductServiceExceptions  {

     @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleProductNotFoundException(ProductNotFoundException ex){

        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }

     @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidProductException(InvalidProductException ex){

        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return ResponseEntity.status(400).body(response);}

}
