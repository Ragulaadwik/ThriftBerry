package com.thriftBerry.orderService.consumer;

import com.thriftBerry.orderService.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;


@Component
public class KafkaOrderConsumer {

private static final Logger log = LoggerFactory.getLogger(KafkaOrderConsumer.class);


private final OrderService orderService;

    public KafkaOrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "reserve-success", groupId = "order_group")
    public void consumeSuccessEvent(String orderId) {
        log.info(" Success Message received -> {}", orderId);
        orderService.initiatePayment(orderId);

    }

    @KafkaListener(topics = "reserve-failure", groupId = "order_group")
    public void consumeFailureEvent(String message) {

        log.info(" Failure Message received -> {}", message);
    }



    @KafkaListener(topics = "payment-success",groupId = "order-group")
    public void consumePaymentSuccessEvent(String orderId){
        log.info("Payment Success Message received -> {}", orderId);
        orderService.confirmOrder(orderId);
    }

    @KafkaListener(topics = "payment-failure",groupId = "order-group")
    public void consumePaymentFailureEvent(String orderId){
        log.info("Payment Failure Message received -> {}", orderId);
        orderService.cancelOrder(Long.valueOf(orderId));
    }


}
