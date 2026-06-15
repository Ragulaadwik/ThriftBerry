package com.thriftBerry.orderService.service;

import com.thriftBerry.orderService.communication.CartClient;
import com.thriftBerry.orderService.dto.OrderRequest;
import com.thriftBerry.orderService.dto.OrderResponse;
import com.thriftBerry.orderService.dto.cart.CartResponse;
import com.thriftBerry.orderService.exception.InvalidUserException;
import com.thriftBerry.orderService.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartClient cartClient;

    public OrderService(OrderRepository orderRepository, CartClient cartClient) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
    }

    public CartResponse placeOrder(OrderRequest orderRequest) {
        log.debug("Request processing for placing order for userId :{}",orderRequest.getUserId());
        //validate userId
        validateUserId(orderRequest.getUserId());

        //get Cart by userId from CartService by feign client

        return cartClient.getCart(orderRequest.getUserId());
    }

    private  void validateUserId(Long userId) {
        if (userId == null) {
            log.error("User ID cannot be null");
            throw new InvalidUserException("User ID cannot be null");
        }
        if (userId < 0) {
            log.error("User ID cannot be negative. Provided: {}",userId);
            throw new InvalidUserException("User ID cannot be negative. Provided: " + userId);
        }
    }
}
