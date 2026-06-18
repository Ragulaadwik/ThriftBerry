package com.thriftBerry.orderService.controller;

import com.thriftBerry.orderService.dto.OrderList;
import com.thriftBerry.orderService.dto.OrderRequest;
import com.thriftBerry.orderService.dto.OrderResponse;
import com.thriftBerry.orderService.dto.cart.CartResponse;
import com.thriftBerry.orderService.entity.Order;
import com.thriftBerry.orderService.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Request received to place order for userId :{}", orderRequest.getUserId());
        OrderResponse response = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        log.info("Request received to get order for orderId :{}", orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<OrderList> getOrdersByUserId(@PathVariable Long userId) {
        log.info("Request received to get orders for userId :{}", userId);

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId){
        log.info("Request received to cancel order for userId :{}",orderId);
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
