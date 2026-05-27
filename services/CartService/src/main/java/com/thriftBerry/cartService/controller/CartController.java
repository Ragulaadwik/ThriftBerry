package com.thriftBerry.cartService.controller;

import com.thriftBerry.cartService.dto.CartItemRequest;

import com.thriftBerry.cartService.model.Cart;
import com.thriftBerry.cartService.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {


    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }





    @PostMapping
    public String addToCart(@RequestBody @Valid CartItemRequest cartItemRequest){

       log.info("Received add to cart request: UserId={}, ProductId={}, Quantity={}",
               cartItemRequest.getUserId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());
            return cartService.addToCart(cartItemRequest);
    }

    @GetMapping
    public ResponseEntity<Cart> getCartByUserId(@RequestParam Long userId) {
        log.info("Received request to get cart for userId: {}", userId);
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            log.warn("No cart found for userId: {}", userId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cart);
    }

}
