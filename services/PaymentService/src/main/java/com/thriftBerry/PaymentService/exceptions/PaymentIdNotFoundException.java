package com.thriftBerry.PaymentService.exceptions;

public class PaymentIdNotFoundException extends RuntimeException {

    public PaymentIdNotFoundException(String message) {
        super(message);
    }

}
