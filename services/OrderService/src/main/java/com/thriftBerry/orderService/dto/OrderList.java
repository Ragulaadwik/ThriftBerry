package com.thriftBerry.orderService.dto;

import java.util.List;

public class OrderList {
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderResponse> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderResponse> orderResponses) {
        this.orderList = orderResponses;
    }

    private Long userId;
    private List<OrderResponse> orderList;
    private String message;
    private int NumberOfOrders;

    public int getNumberOfOrders() {
        return NumberOfOrders;
    }

    public void setNumberOfOrders(int numberOfOrders) {
        NumberOfOrders = numberOfOrders;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
