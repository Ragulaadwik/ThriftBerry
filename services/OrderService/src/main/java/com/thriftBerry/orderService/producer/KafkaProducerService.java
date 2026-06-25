package com.thriftBerry.orderService.producer;

import com.thriftBerry.orderService.dto.OrderCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.LogManager;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


    public KafkaProducerService(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreateEvent(OrderCreatedEvent event){
        try{
            log.info("Publishing OrderCreatedEvent for orderId: {}", event.getOrderId());
            kafkaTemplate.send("order-events", event);
        } catch (Exception ex) {
            log.error("Failed to publish OrderCreatedEvent for orderId {}: {}", event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
