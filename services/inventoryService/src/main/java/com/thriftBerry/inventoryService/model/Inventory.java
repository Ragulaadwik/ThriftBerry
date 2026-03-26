package com.thriftBerry.inventoryService.model;

import jakarta.persistence.*;


@Entity
@Table(
        name = "inventory",
        indexes = {
                @Index(name = "idx_product_id", columnList = "productId")
        }
)
public class Inventory{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Column(unique = true)
    private Long productId;

    private Long availableStock;
    private Long reservedStock;

    public void reserveStock(Long quantity) {
        validateQuantity(quantity);

        if (this.availableStock < quantity) {
            throw new IllegalArgumentException("Insufficient available stock");
        }

        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    public void releaseStock(Long quantity) {
        validateQuantity(quantity);

        if (this.reservedStock < quantity) {
            throw new IllegalArgumentException("Cannot release more than reserved stock");
        }

        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }

    public void confirmStock(Long quantity) {
        validateQuantity(quantity);

        if (this.reservedStock < quantity) {
            throw new IllegalArgumentException("Not enough reserved stock to confirm");
        }

        this.reservedStock -= quantity;
    }

    private void validateQuantity(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}
