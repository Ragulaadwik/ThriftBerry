package com.thriftBerry.orderService.dto;

public class OrderRequest {
    private Long userId;

    public OrderRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public OrderRequest(Long userId) {
        this.userId = userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
