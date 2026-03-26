package com.thriftBerry.inventoryService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;


public class RequestDto {

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Long availableStock) {
        this.availableStock = availableStock;
    }

    public Long getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Long reservedStock) {
        this.reservedStock = reservedStock;
    }

    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Available stock cannot be null")
    @Positive(message = "Available stock must be positive")
    private Long availableStock;


    private Long reservedStock= 0L;
}
