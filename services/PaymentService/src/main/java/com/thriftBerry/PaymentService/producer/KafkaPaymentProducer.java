package com.thriftBerry.PaymentService.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPaymentProducer {

private static final Logger log = LoggerFactory.getLogger(KafkaPaymentProducer.class);
private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPaymentProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccessEvent(String message){

        try {
            log.info("Publishing Payment success Event: {}", message);
            kafkaTemplate.send("payment-success", message);
        }catch (Exception ex){
            log.error("Failed to publish Payment success Event: {}", message, ex);
        }

    }

    public void publishPaymentFailureEvent(String message){

        try {
            log.info("Publishing Payment Failure Event: {}", message);
            kafkaTemplate.send("payment-failure", message);
        }catch (Exception ex){
            log.error("Failed to publish Payment failure Event: {}", message, ex);
        }

    }
}
