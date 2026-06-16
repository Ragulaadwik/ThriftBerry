package com.thriftBerry.orderService.service;

import com.thriftBerry.orderService.communication.CartClient;
import com.thriftBerry.orderService.communication.InventoryClient;
import com.thriftBerry.orderService.dto.OrderList;
import com.thriftBerry.orderService.dto.OrderRequest;
import com.thriftBerry.orderService.dto.OrderResponse;

import com.thriftBerry.orderService.dto.cart.CartResponse;
import com.thriftBerry.orderService.dto.inventory.InventoryRequest;
import com.thriftBerry.orderService.exception.InvalidUserException;
import com.thriftBerry.orderService.mapper.OrderItemMapper;
import com.thriftBerry.orderService.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public OrderService(OrderRepository orderRepository, CartClient cartClient, InventoryClient inventoryClient, OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.orderItemMapper = orderItemMapper;
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
            reserved = reserveInventoryForCart(cartResponse);

            // calculate total
            BigDecimal totalAmount = calculateTotal(cartResponse);
            log.info("Calculated total amount {} for userId {}", totalAmount, orderRequest.getUserId());

            // build and persist order
            Order order = buildOrderEntity(orderRequest, totalAmount);
            List<OrderItem> orderItems = mapCartItemToOrderItem(cartResponse, order);
            order.setOrderItems(orderItems);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            Order saved = orderRepository.save(order);
            saved.setUpdatedAt(LocalDateTime.now());
            // business decision: after successful persistence, confirm inventory
            confirmInventoryForReserved(reserved);

            clearCart(orderRequest.getUserId());

            response.setOrderId(saved.getOrderId());
            response.setTotalAmount(saved.getTotalAmount());
            response.setStatus(saved.getOrderStatus().name());
            response.setOrderItems(orderItemMapper.toItemResponseList(saved.getOrderItems()));
            response.setMessage("Order placed successfully");
            return response;
        } catch (Exception ex) {
            log.error("Failed to place order for userId {}: {}", orderRequest.getUserId(), ex.getMessage(), ex);
            // attempt best-effort release of any previously reserved inventory
            try {
                if (!reserved.isEmpty()) {
                    releaseReservedInventory(reserved);
                }
            } catch (Exception releaseEx) {
                log.error("Failed to release inventory after order failure for userId {}: {}", orderRequest.getUserId(), releaseEx.getMessage(), releaseEx);
            }

            response.setMessage("Failed to place order: " + ex.getMessage());
            return response;
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
        Map<Long, Long> reserved = new HashMap<>();
        for (var ci : cartResponse.getCartItems()) {
            if (ci == null || ci.getProductId() == null || ci.getQuantity() == null) {
                log.warn("Skipping invalid cart item while reserving inventory: {}", ci);
                continue;
            }

            InventoryRequest req = new InventoryRequest();
            req.setProductId(ci.getProductId());
            // CartItem.quantity is Long; InventoryRequest.quantity is primitive long
            long qty = ci.getQuantity();
            req.setQuantity(qty);

            InventoryResponse resp;
            try {
                resp = inventoryClient.reserveInventory(req);
            } catch (Exception ex) {
                log.error("Inventory reserve call failed for productId {} qty {}: {}", ci.getProductId(), qty, ex.getMessage(), ex);
                // throw to trigger release of previously reserved
                throw ex;
            }

            // Basic validation of reservation
            if (resp == null || resp.getReservedQuantity() < qty) {
                log.error("Inventory reservation insufficient for productId {}. requested={}, reservedResp={}", ci.getProductId(), qty, resp);
                throw new IllegalStateException("Failed to reserve inventory for productId " + ci.getProductId());
            }

            reserved.put(ci.getProductId(), qty);
            log.debug("Reserved inventory for productId {} qty {}", ci.getProductId(), qty);
        }
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
}
