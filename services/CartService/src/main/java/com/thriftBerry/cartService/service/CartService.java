package com.thriftBerry.cartService.service;

import com.thriftBerry.cartService.dto.CartItemRequest;

public interface CartService {

    String addToCart(CartItemRequest request);
}
