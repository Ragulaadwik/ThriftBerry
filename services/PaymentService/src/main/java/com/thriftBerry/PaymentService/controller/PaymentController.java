package com.thriftBerry.PaymentService.controller;

import com.thriftBerry.PaymentService.dto.PaymentResponse;
import com.thriftBerry.PaymentService.service.PaymentService;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

     private static final Logger log = org.slf4j.LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String testPayment() {
        return "Payment Service is working!";
    }
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById( @PathVariable Long paymentId) {
         log.info("Request received to get payment for paymentId :{}", paymentId);
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }


}
