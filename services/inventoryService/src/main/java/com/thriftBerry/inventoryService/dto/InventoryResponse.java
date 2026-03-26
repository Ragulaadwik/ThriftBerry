package com.thriftBerry.inventoryService.dto;

public class InventoryResponse {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Long productId;

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

    private Long availableStock;
    private Long reservedStock;
}
