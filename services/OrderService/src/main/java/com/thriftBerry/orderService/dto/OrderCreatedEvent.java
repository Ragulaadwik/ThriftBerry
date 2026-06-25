package com.thriftBerry.orderService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {

    public Long getOrderId() {
        return orderId;
    }

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, Long userId, List<OrderItemEvent> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderItems = orderItems;

    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItemEvent> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemEvent> orderItems) {
        this.orderItems = orderItems;
    }





    private Long orderId;
    private Long userId;
    private List<OrderItemEvent> orderItems;

}
