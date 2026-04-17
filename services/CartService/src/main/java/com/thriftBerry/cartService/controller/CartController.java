package com.thriftBerry.cartService.controller;

import com.thriftBerry.cartService.dto.CartItemRequest;

import com.thriftBerry.cartService.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {


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

////        return productClient.getProductById(cartItemRequest.getProductId());
//        System.out.println(inventoryClient.checkAvailability(cartItemRequest.getProductId(),(Long) (cartItemRequest.getQuantity()).longValue()));
//          return inventoryClient.checkAvailability(cartItemRequest.getProductId(),(Long) (cartItemRequest.getQuantity()).longValue());
            return cartService.addToCart(cartItemRequest);
    }

}
