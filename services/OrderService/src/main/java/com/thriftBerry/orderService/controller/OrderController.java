package com.thriftBerry.orderService.controller;

import com.thriftBerry.orderService.dto.OrderRequest;
import com.thriftBerry.orderService.dto.OrderResponse;
import com.thriftBerry.orderService.dto.cart.CartResponse;
import com.thriftBerry.orderService.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest){
        log.info("Request received to place order for userId :{}",orderRequest.getUserId());
        OrderResponse response = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(response);
    }
}
