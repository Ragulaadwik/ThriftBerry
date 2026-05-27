package com.thriftBerry.cartService.controller;

import com.thriftBerry.cartService.dto.CartItemRequest;

import com.thriftBerry.cartService.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {


    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @GetMapping
    public String viewCart(){
        return "Hello Cart!";
    }


    @PostMapping
    public String addToCart(@RequestBody @Valid CartItemRequest cartItemRequest){

       log.info("Received add to cart request: UserId={}, ProductId={}, Quantity={}",
               cartItemRequest.getUserId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());
            return cartService.addToCart(cartItemRequest);
    }

}
