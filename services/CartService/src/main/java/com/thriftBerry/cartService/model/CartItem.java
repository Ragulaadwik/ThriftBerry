package com.thriftBerry.cartService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;



@Entity
public class CartItem {
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    private Long productId;
    private Long quantity;



    @ManyToOne
    @JoinColumn(name ="cartId")
    @JsonIgnore
    private Cart cart;

    public void setCart(Cart cart){
        this.cart = cart;
    }

    public Cart getCart(){
        return cart;
    }
}
