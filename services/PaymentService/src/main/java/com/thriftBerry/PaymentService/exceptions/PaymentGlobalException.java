package com.thriftBerry.PaymentService.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentGlobalException {
    @ExceptionHandler(PaymentIdNotFoundException.class)
    public ResponseEntity<String> handlePaymentIdNotFoundException(PaymentIdNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
