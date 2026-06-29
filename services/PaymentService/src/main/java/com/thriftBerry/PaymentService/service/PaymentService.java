package com.thriftBerry.PaymentService.service;

import com.thriftBerry.PaymentService.communication.OrderClient;
import com.thriftBerry.PaymentService.dto.OrderResponse;
import com.thriftBerry.PaymentService.dto.PaymentResponse;
import com.thriftBerry.PaymentService.enums.PaymentStatus;
import com.thriftBerry.PaymentService.exceptions.PaymentIdNotFoundException;
import com.thriftBerry.PaymentService.model.Payment;
import com.thriftBerry.PaymentService.producer.KafkaPaymentProducer;
import com.thriftBerry.PaymentService.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final KafkaPaymentProducer producer;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient, KafkaPaymentProducer producer) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.producer = producer;
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment details for paymentId: {}", paymentId);
        Optional<Payment> saved = paymentRepository.findById(paymentId);

        if (saved.isPresent()) {
            Payment payment = saved.get();
            return mapToPaymentResponse(payment);
        } else {
            log.warn("Payment not found for paymentId: {}", paymentId);
            throw new PaymentIdNotFoundException("Payment not found for paymentId: " + paymentId);
        }
    }


    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        return response;
    }

    public void processPayment(String orderId) {
        log.info("Processing payment for orderId: {}", orderId);
        Payment payment = new Payment();
        payment.setOrderId(Long.parseLong(orderId));
        OrderResponse orderResponse = orderClient.getOrder(Long.valueOf(orderId));

        payment.setUserId(orderResponse.getUserId());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setAmount(orderClient.getOrder(Long.valueOf(orderId)).getTotalAmount());
       boolean paymentSuccess = new Random().nextBoolean();
        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            Payment savedPayment = paymentRepository.save(payment);
            producer.publishPaymentSuccessEvent(savedPayment.getOrderId().toString());
            log.info("Payment successful for orderId: {}", orderId);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            Payment savedPayment = paymentRepository.save(payment);
            producer.publishPaymentFailureEvent(savedPayment.getOrderId().toString());
            log.warn("Payment failed for orderId: {}", orderId);
        }



    }
}
