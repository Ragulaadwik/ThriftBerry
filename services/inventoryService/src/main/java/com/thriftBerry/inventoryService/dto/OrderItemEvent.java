package com.thriftBerry.inventoryService.dto;



public class OrderItemEvent {
    private Long productId;
    private Long quantity;

    @Override
    public String toString() {
        return "OrderItemEvent{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }

    public Long getProductId() {
        return productId;
    }

    public OrderItemEvent() {
    }

    public OrderItemEvent(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
