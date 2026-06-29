package com.thriftBerry.PaymentService.model;

import com.thriftBerry.PaymentService.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long orderId;

    public Payment() {
    }

    public Payment(Long id, Long userId, Long orderId, BigDecimal amount, String transactionalId, LocalDateTime createdAt, PaymentStatus status) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionalId = transactionalId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public void setTransactionalId(String transactionalId) {
        this.transactionalId = transactionalId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    private BigDecimal amount;
    private String transactionalId;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
