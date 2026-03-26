package com.thriftBerry.inventoryService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class InventoryBookingRequest {
    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private long quantity;
}
