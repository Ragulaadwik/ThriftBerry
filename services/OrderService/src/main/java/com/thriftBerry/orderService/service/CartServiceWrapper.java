package com.thriftBerry.orderService.service;

import com.thriftBerry.orderService.communication.CartClient;
import com.thriftBerry.orderService.dto.cart.CartResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CartServiceWrapper {

    private final CartClient cartClient;
    private  static  final Logger log = LoggerFactory.getLogger(CartServiceWrapper.class);
    public CartServiceWrapper(CartClient cartClient) {
        this.cartClient = cartClient;
    }

    @CircuitBreaker(name = "fetchCart",fallbackMethod = "fetchCartFallback")
    @Retry(name = "fetchCart")
    public CartResponse fetchCart(Long userId) {
        try {
            return cartClient.getCart(userId);
        } catch (Exception ex) {
            log.error("Error fetching cart for userId {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public CartResponse fetchCartFallback(Long userId,Exception ex){
        log.error("FetchCart Fallback Activated : Error fetching cart for userId :{}",userId);
        CartResponse response = new CartResponse();
        response.setCartItems(null);
        response.setUserId(userId);
        return response;
    }

    @CircuitBreaker(name = "cartService",fallbackMethod = "clearCartFallback")
    @Retry(name = "cartService")
    public void clearCart(Long userId) {
        try {
            cartClient.clearCart(userId);
            log.info("Cleared cart for userId {}", userId);
        } catch (Exception ex) {
            log.error("Failed to clear cart for userId {}: {}", userId, ex.getMessage(), ex);

        }
    }

    public void clearCartFallback(Long userId,Exception ex){
        log.error("clearCartFallback activated : Cart service unavailable for userId :{}",userId);
    }





}
