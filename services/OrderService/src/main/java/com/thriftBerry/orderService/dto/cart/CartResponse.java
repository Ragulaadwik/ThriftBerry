package com.thriftBerry.orderService.dto.cart;



import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    private Long userId;

    private List<CartItem> cartItems = new ArrayList<>();



}
