package com.thriftBerry.PaymentService.consumer;

import com.thriftBerry.PaymentService.service.PaymentService;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentConsumer {
Logger log = org.slf4j.LoggerFactory.getLogger(KafkaPaymentConsumer.class);
private final PaymentService paymentService;

    public KafkaPaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "initiate-payment", groupId = "payment_group")
    public void consumeSuccessEvent(String orderId) {
        log.info(" Success Message received -> {}", orderId);
        paymentService.processPayment(orderId);

    }

}
