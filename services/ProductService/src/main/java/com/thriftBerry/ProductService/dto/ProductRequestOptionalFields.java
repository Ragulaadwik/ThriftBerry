package com.thriftBerry.ProductService.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequestOptionalFields {
    @Size(min = 1, message = "Product name cannot be empty")
    private String productName;

    @Size(min = 1, message = "Product description cannot be empty")
    private String productDescription;

    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(min = 1, message = "Category cannot be empty")
    private String category;



    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



}
