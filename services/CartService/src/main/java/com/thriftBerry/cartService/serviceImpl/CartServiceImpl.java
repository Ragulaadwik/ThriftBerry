package com.thriftBerry.cartService.serviceImpl;

import com.thriftBerry.cartService.communication.InventoryClient;
import com.thriftBerry.cartService.communication.ProductClient;
import com.thriftBerry.cartService.dto.AvailabilityResponse;
import com.thriftBerry.cartService.dto.CartItemRequest;

import com.thriftBerry.cartService.model.Cart;
import com.thriftBerry.cartService.model.CartItem;

import com.thriftBerry.cartService.repository.CartRepository;
import com.thriftBerry.cartService.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    private final CartRepository cartRepository;

    public CartServiceImpl(ProductClient productClient, InventoryClient inventoryClient,  CartRepository cartRepository) {
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;

        this.cartRepository = cartRepository;
    }


    @Override
    @Transactional
    public String addToCart(CartItemRequest request) {

        if(request.getQuantity()<=0){
            log.error("Invalid quantity: {} for productId: {} by userId: {}", request.getQuantity(), request.getProductId(), request.getUserId());
            throw new RuntimeException("Quantity must be at least one");
        }

        productClient.getProductById(request.getProductId());

        AvailabilityResponse response = inventoryClient.checkAvailability(request.getProductId(),request.getQuantity());


         if(Boolean.FALSE.equals(response.getAvailable())) {
             log.error("Insufficient stock for productId: {} requested quantity: {} by userId: {}. Available quantity: {}",
                     request.getProductId(), request.getQuantity(), request.getUserId(), response.getAvailableQuantity());
             throw new RuntimeException("Insufficient stock: Available Quantity:"+response.getAvailableQuantity());
         }

         log.info("Adding to cart: userId={}, productId={}, quantity={}", request.getUserId(), request.getProductId(), request.getQuantity());

        Cart cart = cartRepository.findByUserId(request.getUserId()).orElseGet(()->{
            Cart newCart = new Cart();
            newCart.setUserId(request.getUserId());
            return newCart;
        });

        Optional<CartItem> existingItem = cart.getCartItems().stream().
                filter(item -> item.getProductId().equals(request.getProductId())).findFirst();

        if(existingItem.isPresent()){

            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity()+ request.getQuantity());
        }else{
            CartItem newItem = new CartItem();
            newItem.setQuantity(request.getQuantity());
            newItem.setProductId(request.getProductId());
            cart.additem(newItem);
        }

        cartRepository.save(cart);
        log.info("Cart updated successfully for userId: {}. ProductId: {}, Quantity: {}", request.getUserId(), request.getProductId(), request.getQuantity());
         return "Item saved to cart ";
    }
}
