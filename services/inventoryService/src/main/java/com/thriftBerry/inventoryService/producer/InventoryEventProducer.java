package com.thriftBerry.inventoryService.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSuccessEvent(String orderId) {
        log.info("Publishing reservation success event for order ID: {}", orderId);
        kafkaTemplate.send("reserve-success",  orderId);
    }

    public void publishFailureEvent(String orderId, String errorMessage) {
        log.info("Publishing reservation failure event for order ID: {}", orderId);
        kafkaTemplate.send("reserve-failure",  orderId + ". Error: " + errorMessage);
    }
}
