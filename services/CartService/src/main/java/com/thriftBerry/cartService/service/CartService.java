package com.thriftBerry.cartService.service;

import com.thriftBerry.cartService.dto.CartItemRequest;
import com.thriftBerry.cartService.model.Cart;
import jakarta.validation.Valid;

/**
 * Service interface for cart operations.
 * Provides methods to manage shopping cart items and retrieve cart information.
 */
public interface CartService {

    /**
     * Adds an item to the cart for a specific user.
     *
     * @param request the cart item request containing product ID, quantity, and user ID
     * @return a success message if the item was added
     * @throws com.thriftBerry.cartService.exceptions.InvalidInputException if the request is invalid
     * @throws RuntimeException if the product is not available or stock is insufficient
     */
    String addToCart(CartItemRequest request);

    /**
     * Retrieves the cart for a specific user.
     *
     * @param userId the user ID
     * @return Optional containing the cart if found, empty Optional otherwise
     * @throws com.thriftBerry.cartService.exceptions.InvalidInputException if userId is invalid
     */
    Cart getCartByUserId(Long userId);

    Cart updateCartItem(@Valid CartItemRequest cartItemRequest);
}
