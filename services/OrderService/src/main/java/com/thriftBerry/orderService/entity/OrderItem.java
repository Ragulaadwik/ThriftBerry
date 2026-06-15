package com.thriftBerry.orderService.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderItems")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;
    private Long productId;

    public OrderItem() {
    }

    public OrderItem(Long orderItemId, Long productId, Long quantity, BigDecimal price, Order order) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.order = order;
    }

    private Long quantity;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    private BigDecimal price;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
