package com.thriftBerry.inventoryService.consumer;

import com.thriftBerry.inventoryService.dto.InventoryBookingRequest;
import com.thriftBerry.inventoryService.dto.OrderCreatedEvent;
import com.thriftBerry.inventoryService.dto.OrderItemEvent;
import com.thriftBerry.inventoryService.producer.InventoryEventProducer;
import com.thriftBerry.inventoryService.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);
    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryConsumer(InventoryService inventoryService, InventoryEventProducer inventoryEventProducer) {
        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consume(OrderCreatedEvent event) {
        if (ObjectUtils.isEmpty(event) || ObjectUtils.isEmpty(event.getOrderItems())) {
            log.warn("Received an empty or invalid OrderCreatedEvent: {}", event);
            return;
        }

        log.info("Processing OrderCreatedEvent with {} items", event.getOrderItems().size());

        boolean allSuccess = true;
        StringBuilder failureMessages = new StringBuilder();

        for (OrderItemEvent item : event.getOrderItems()) {
            try {
                log.info("Attempting to reserve inventory for product ID: {} and Quantity: {}", item.getProductId(), item.getQuantity());
                inventoryService.reserveInventory(new InventoryBookingRequest(item.getProductId(), item.getQuantity()));
                log.info("Successfully reserved inventory for product ID: {}", item.getProductId());
            } catch (Exception e) {
                allSuccess = false;
                String errorMessage = String.format("Failed to reserve inventory for product ID: %s. Error: %s", item.getProductId(), e.getMessage());
                log.error(errorMessage, e);
                failureMessages.append(errorMessage).append("; ");
            }
        }

        if (allSuccess) {
            inventoryEventProducer.publishSuccessEvent(event.getOrderId().toString());
        } else {
            inventoryEventProducer.publishFailureEvent(event.getOrderId().toString(), failureMessages.toString());
        }
    }
}
