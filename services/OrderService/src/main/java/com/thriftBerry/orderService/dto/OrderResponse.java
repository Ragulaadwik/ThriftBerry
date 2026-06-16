package com.thriftBerry.orderService.dto;

import java.math.BigDecimal;

/**
 * Response returned when an order is placed. Contains summary information suitable for clients.
 */
public class OrderResponse {

    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private String message;

    public OrderResponse() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
