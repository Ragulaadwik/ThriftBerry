package com.thriftBerry.PaymentService.dto;

import com.thriftBerry.PaymentService.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentResponse {
    private Long paymentId;
    private Long userId;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentResponse(Long paymentId, Long userId, Long orderId, PaymentStatus status, BigDecimal amount) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    private Long orderId;
    private PaymentStatus status;

    public PaymentResponse() {
    }

    public PaymentResponse(Long userId, Long orderId, PaymentStatus status, BigDecimal amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    private BigDecimal amount;

}
