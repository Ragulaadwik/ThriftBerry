package com.thriftBerry.orderService.service;

import com.thriftBerry.orderService.communication.CartClient;
import com.thriftBerry.orderService.communication.InventoryClient;
import com.thriftBerry.orderService.dto.OrderCreatedEvent;
import com.thriftBerry.orderService.dto.OrderList;
import com.thriftBerry.orderService.dto.OrderRequest;
import com.thriftBerry.orderService.dto.OrderResponse;

import com.thriftBerry.orderService.dto.cart.CartResponse;
import com.thriftBerry.orderService.dto.inventory.InventoryRequest;
import com.thriftBerry.orderService.exception.InvalidUserException;
import com.thriftBerry.orderService.mapper.OrderItemMapper;
import com.thriftBerry.orderService.producer.KafkaProducerService;
import com.thriftBerry.orderService.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.thriftBerry.orderService.dto.inventory.InventoryResponse;

import org.springframework.transaction.annotation.Transactional;

import com.thriftBerry.orderService.entity.Order;
import com.thriftBerry.orderService.entity.OrderItem;
import com.thriftBerry.orderService.enums.OrderStatus;

@Service
public class OrderService {






    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final OrderItemMapper orderItemMapper;
    private final KafkaProducerService kafkaProducerService;

    public OrderService(OrderRepository orderRepository, CartClient cartClient, InventoryClient inventoryClient, OrderItemMapper orderItemMapper, KafkaProducerService kafkaProducerService) {


        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.orderItemMapper = orderItemMapper;
        this.kafkaProducerService = kafkaProducerService;
    }


    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        log.debug("Request processing for placing order for userId :{}", orderRequest.getUserId());

        // validate userId early
        validateUserId(orderRequest.getUserId());

        OrderResponse response = new OrderResponse();

        // Fetch cart
        CartResponse cartResponse = fetchCart(orderRequest.getUserId());
        if (cartResponse == null || cartResponse.getCartItems() == null || cartResponse.getCartItems().isEmpty()) {
            log.warn("Cart is empty for userId: {}", orderRequest.getUserId());
            response.setMessage("Cart is empty");
            return response;
        }

        // Reserve inventory for all cart items. Keep track of successfully reserved items so we can release on error.
        Map<Long, Long> reserved = new HashMap<>(); // productId -> quantity
        try {



            // calculate total
            BigDecimal totalAmount = calculateTotal(cartResponse);
            log.info("Calculated total amount {} for userId {}", totalAmount, orderRequest.getUserId());

            // build and persist order
            Order order = buildOrderEntity(orderRequest, totalAmount);
            List<OrderItem> orderItems = mapCartItemToOrderItem(cartResponse, order);
            order.setOrderItems(orderItems);
            //payment logic in future
            order.setOrderStatus(OrderStatus.PENDING);
            Order saved = orderRepository.save(order);

            publishKafka(saved);
            saved.setUpdatedAt(LocalDateTime.now());
            // business decision: after successful persistence, confirm inventory
//            confirmInventoryForReserved(reserved);



            response.setOrderId(saved.getOrderId());
            response.setTotalAmount(saved.getTotalAmount());
            response.setStatus(saved.getOrderStatus().name());
            response.setOrderItems(orderItemMapper.toItemResponseList(saved.getOrderItems()));
            response.setMessage("Order placed successfully");
            return response;
        } catch (Exception ex) {
            log.error("Failed to place order for userId {}: {}", orderRequest.getUserId(), ex.getMessage(), ex);
            // attempt best-effort release of any previously reserved inventory


            response.setMessage("Failed to place order: " + ex.getMessage());
            return response;
        }
    }

    private void publishKafka(Order saved) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderItems(orderItemMapper.toOrderItemEventList(saved.getOrderItems()));
        event.setUserId(saved.getUserId());
        event.setOrderId(saved.getOrderId());
        try{
            kafkaProducerService.publishOrderCreateEvent(event);
        } catch (Exception ex) {
            log.error("Failed to publish OrderCreatedEvent for userId {}: {}", saved.getUserId(), ex.getMessage(), ex);
        }

    }

    private void clearCart(Long userId) {
        try {
            cartClient.clearCart(userId);
            log.info("Cleared cart for userId {}", userId);
        } catch (Exception ex) {
            log.error("Failed to clear cart for userId {}: {}", userId, ex.getMessage(), ex);

        }
    }

    private CartResponse fetchCart(Long userId) {
        try {
            return cartClient.getCart(userId);
        } catch (Exception ex) {
            log.error("Error fetching cart for userId {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    private Map<Long, Long> reserveInventoryForCart(CartResponse cartResponse) {

        OrderCreatedEvent event = new OrderCreatedEvent();

        event.setUserId(cartResponse.getUserId());

//        event.setOrderItems(orderItemMapper.toOrderItemEventList(cartResponse.getCartItems()));

//        kafkaTemplate.send("inventory-reserve-topic","OrderService message");
        log.info("Sent inventory reserve event for userId {}: {}", cartResponse.getUserId(), event);
        // In a real-world scenario, we would wait for a response or confirmation from the inventory service before proceeding. For this example, we assume the reservation is successful.
        // Here, we return a map of productId to quantity for the reserved items. In a real implementation,




        Map<Long, Long> reserved = new HashMap<>();

//        for (var ci : cartResponse.getCartItems()) {
//            if (ci == null || ci.getProductId() == null || ci.getQuantity() == null) {
//                log.warn("Skipping invalid cart item while reserving inventory: {}", ci);
//                continue;
//            }
//
//            InventoryRequest req = new InventoryRequest();
//            req.setProductId(ci.getProductId());
//            // CartItem.quantity is Long; InventoryRequest.quantity is primitive long
//            long qty = ci.getQuantity();
//            req.setQuantity(qty);
//
//            InventoryResponse resp;
//            try {
//                resp = inventoryClient.reserveInventory(req);
//            } catch (Exception ex) {
//                log.error("Inventory reserve call failed for productId {} qty {}: {}", ci.getProductId(), qty, ex.getMessage(), ex);
//                // throw to trigger release of previously reserved
//                throw ex;
//            }
//
//            // Basic validation of reservation
//            if (resp == null || resp.getReservedQuantity() < qty) {
//                log.error("Inventory reservation insufficient for productId {}. requested={}, reservedResp={}", ci.getProductId(), qty, resp);
//                throw new IllegalStateException("Failed to reserve inventory for productId " + ci.getProductId());
//            }
//
//            reserved.put(ci.getProductId(), qty);
//            log.debug("Reserved inventory for productId {} qty {}", ci.getProductId(), qty);
//        }
        return reserved;
    }

    private void releaseReservedInventory(Map<Long, Long> reserved) {
        if (reserved == null || reserved.isEmpty()) return;
        for (Map.Entry<Long, Long> e : reserved.entrySet()) {
            InventoryRequest req = new InventoryRequest();
            req.setProductId(e.getKey());
            req.setQuantity(e.getValue());
            try {
                inventoryClient.releaseInventory(req);
                log.debug("Released inventory for productId {} qty {}", e.getKey(), e.getValue());
            } catch (Exception ex) {
                log.warn("Failed to release inventory for productId {} qty {}: {}", e.getKey(), e.getValue(), ex.getMessage());
            }
        }
    }

    private void confirmInventoryForReserved(Map<Long, Long> reserved) {
        if (reserved == null || reserved.isEmpty()) return;
        for (Map.Entry<Long, Long> e : reserved.entrySet()) {
            InventoryRequest req = new InventoryRequest();
            req.setProductId(e.getKey());
            req.setQuantity(e.getValue());
            try {
                InventoryResponse resp = inventoryClient.confirmInventory(req);
                log.debug("Confirmed inventory for productId {} qty {} response={}", e.getKey(), e.getValue(), resp != null ? resp.getMessage() : null);
            } catch (Exception ex) {
                // confirm failures are serious but order is persisted; log and continue. Consider async retry in production.
                log.error("Failed to confirm inventory for productId {} qty {}: {}", e.getKey(), e.getValue(), ex.getMessage(), ex);
            }
        }
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

    // old MapCartItemToOrderItem removed; use mapCartItemToOrderItem below

    private BigDecimal calculateTotal(CartResponse cartResponse) {
        return cartResponse.getCartItems().stream()
                .filter(item -> item != null && item.getPrice() != null && item.getQuantity() != null)
                .map(item -> BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> mapCartItemToOrderItem(CartResponse cartResponse, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        cartResponse.getCartItems().stream()
                .filter(item -> item != null && item.getPrice() != null && item.getQuantity() != null)
                .forEach(ci -> {
                    OrderItem oi = new OrderItem();
                    oi.setProductId(ci.getProductId());
                    oi.setQuantity(ci.getQuantity());
                    // price in CartItem is Long; we store as BigDecimal. NOTE: clarify units (cents vs units) if needed.
                    oi.setPrice(BigDecimal.valueOf(ci.getPrice()));
                    oi.setOrder(order);
                    orderItems.add(oi);
                });
        return orderItems;
    }

    private Order buildOrderEntity(OrderRequest orderRequest,BigDecimal totalAmount){
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public OrderResponse getOrder(Long orderId) {
        OrderResponse response = new OrderResponse();
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                response.setOrderId(order.getOrderId());
                response.setTotalAmount(order.getTotalAmount());
                response.setStatus(order.getOrderStatus().name());
                response.setOrderItems(orderItemMapper.toItemResponseList(order.getOrderItems()));
                log.info("Fetched orderItem for orderId {}: {}", orderId, response);
                response.setMessage("Order retrieved successfully");
            } else {
                response.setMessage("Order not found for orderId " + orderId);
            }
        } catch (Exception ex) {
            log.error("Failed to retrieve order for orderId {}: {}", orderId, ex.getMessage(), ex);
            response.setMessage("Failed to retrieve order: " + ex.getMessage());
        }
        return response;
    }

    public OrderList getOrdersByUserId(Long userId) {

        validateUserId(userId);
        OrderList list = new OrderList();
        list.setUserId(userId);
       List<OrderResponse> response = new ArrayList<>();
           try {
            List<Order> orders = orderRepository.findByUserId(userId);
            if (orders != null && !orders.isEmpty()) {
                for(Order o : orders) {
                    OrderResponse r = new OrderResponse();
                    r.setOrderId(o.getOrderId());
                    r.setTotalAmount(o.getTotalAmount());
                    r.setStatus(o.getOrderStatus().name());
                    r.setOrderItems(orderItemMapper.toItemResponseList(o.getOrderItems()));
                    response.add(r);
                }
                list.setOrderList(response);
                list.setNumberOfOrders(orders.size());
                log.info("Fetched {} orders for userId {}", orders.size(), userId);
                list.setMessage("Orders retrieved successfully");
            } else {
                list.setMessage("No orders found for userId " + userId);
            }
        } catch (Exception ex) {
            log.error("Failed to retrieve orders for userId {}: {}", userId, ex.getMessage(), ex);
            list.setMessage("Failed to retrieve orders: " + ex.getMessage());
        }
        return list;
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.debug("Processing order cancellation for orderId: {}", orderId);

        boolean validOrderId = validateOrderId(orderId);
        OrderResponse response = new OrderResponse();

        if(!validOrderId){
            log.warn("Invalid orderId provided: {}", orderId);
            response.setMessage("Invalid OrderId");
            return response;
        }

        Optional<Order> order = orderRepository.findById(orderId);
        if(order.isEmpty()){
            log.warn("Order not found for orderId: {}", orderId);
            response.setMessage("Order not found for orderId " + orderId);
            return response;
        }
        
        Order orderToCancel = order.get();
        OrderStatus currentStatus = orderToCancel.getOrderStatus();
        log.info("Current order status for orderId {}: {}", orderId, currentStatus);

        // Check if order can be cancelled (only PENDING or CONFIRMED orders can be cancelled)
        if(!OrderStatus.PENDING.equals(currentStatus)){
            log.warn("Cannot cancel order with status {} for orderId {}", currentStatus, orderId);
            response.setMessage("Cannot cancel order with status " + currentStatus.name());
            return response;
        }

        try {
            // Release inventory for all order items
            Map<Long, Long> inventoryToRelease = new HashMap<>();
            if(orderToCancel.getOrderItems() != null && !orderToCancel.getOrderItems().isEmpty()){
                for(OrderItem item : orderToCancel.getOrderItems()){
                    if(item != null && item.getProductId() != null && item.getQuantity() != null){
                        inventoryToRelease.put(item.getProductId(), item.getQuantity());
                        log.debug("Added productId {} quantity {} to release list", item.getProductId(), item.getQuantity());
                    }
                }
            }

            // Release inventory
            if(!inventoryToRelease.isEmpty()){
                releaseReservedInventory(inventoryToRelease);
                log.info("Released inventory for orderId {}", orderId);
            }

            // Update order status to CANCEL
            orderToCancel.setOrderStatus(OrderStatus.CANCELLED);
            orderToCancel.setUpdatedAt(LocalDateTime.now());
            Order saved = orderRepository.save(orderToCancel);

            log.info("Order cancelled successfully for orderId {}", orderId);
            response.setOrderId(saved.getOrderId());
            response.setStatus(saved.getOrderStatus().name());
            response.setTotalAmount(saved.getTotalAmount());
            response.setOrderItems(orderItemMapper.toItemResponseList(saved.getOrderItems()));
            response.setMessage("Order cancelled successfully");
            return response;

        } catch (Exception ex) {
            log.error("Failed to cancel order for orderId {}: {}", orderId, ex.getMessage(), ex);
            response.setMessage("Failed to cancel order: " + ex.getMessage());
            return response;
        }
    }

    private boolean validateOrderId(Long orderId){

        if(orderId==null || orderId<0){
            log.warn("Invalid orderId :{}",orderId);
            return false;
        }

        return true;
    }


}
