package com.thriftBerry.orderService.dto;

import java.math.BigDecimal;

public class ItemResponse {

    private Long productId;
    private Long quantity;
    private BigDecimal price;


    public Long getProductId() {
        return productId;
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



    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }



}
