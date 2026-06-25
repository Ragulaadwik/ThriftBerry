package com.thriftBerry.orderService.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;


@Component
public class KafkaOrderConsumer {

private static final Logger log = LoggerFactory.getLogger(KafkaOrderConsumer.class);

    @KafkaListener(topics = "reserve-success", groupId = "order_group")
    public void consumeSuccessEvent(String message) {
        System.out.println("Consume success Event");
        log.info(" Success Message received -> {}", message);
    }

    @KafkaListener(topics = "reserve-failure", groupId = "order_group")
    public void consumeFailureEvent(String message) {
        System.out.println("consume Failure Event");
        log.info(" Failure Message received -> {}", message);
    }


}
