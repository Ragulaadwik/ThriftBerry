package com.thriftBerry.inventoryService.dto;



import java.util.List;

public class OrderCreatedEvent {


    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", orderItems=" + orderItems +
                '}';
    }

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

