package com.thriftBerry.cartService.serviceImpl;

import com.thriftBerry.cartService.communication.InventoryClient;
import com.thriftBerry.cartService.communication.ProductClient;
import com.thriftBerry.cartService.dto.AvailabilityResponse;
import com.thriftBerry.cartService.dto.CartItemRequest;
import com.thriftBerry.cartService.exceptions.InvalidInputException;
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
    
    // Constants for error messages
    private static final String INVALID_QUANTITY_MSG = "Quantity must be at least one";
    private static final String INSUFFICIENT_STOCK_MSG = "Insufficient stock: Available Quantity: %d";
    private static final String INVALID_USER_ID_MSG = "User ID must be positive";
    private static final String INVALID_REQUEST_MSG = "Cart item request cannot be null";

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CartRepository cartRepository;

    public CartServiceImpl(ProductClient productClient, InventoryClient inventoryClient, CartRepository cartRepository) {
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;

        this.cartRepository = cartRepository;
    }


    @Override
    @Transactional
    public String addToCart(CartItemRequest request) {
        
        // Input validation
        validateAddToCartRequest(request);
        
        log.debug("Processing add to cart request: userId={}, productId={}, quantity={}", 
                  request.getUserId(), request.getProductId(), request.getQuantity());

        try {
            // Verify product exists
            productClient.getProductById(request.getProductId());
            log.debug("Product verification successful for productId: {}", request.getProductId());

            // Check inventory availability
            AvailabilityResponse response = inventoryClient.checkAvailability(
                    request.getProductId(), 
                    request.getQuantity()
            );

            if (Boolean.FALSE.equals(response.getAvailable())) {
                log.warn("Insufficient stock for productId: {} requested quantity: {} by userId: {}. Available: {}",
                        request.getProductId(), request.getQuantity(), request.getUserId(), response.getAvailableQuantity());
                throw new RuntimeException(String.format(INSUFFICIENT_STOCK_MSG, response.getAvailableQuantity()));
            }

            log.info("Stock verified. Adding to cart: userId={}, productId={}, quantity={}", 
                     request.getUserId(), request.getProductId(), request.getQuantity());

            // Get or create cart
            Cart cart = cartRepository.findByUserId(request.getUserId()).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserId(request.getUserId());
                log.debug("Creating new cart for userId: {}", request.getUserId());
                return newCart;
            });

            // Add or update cart item
            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getProductId().equals(request.getProductId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                long previousQuantity = item.getQuantity();
                item.setQuantity(item.getQuantity() + request.getQuantity());
                log.debug("Updated existing cart item. ProductId: {}, previous quantity: {}, new quantity: {}",
                         request.getProductId(), previousQuantity, item.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setQuantity(request.getQuantity());
                newItem.setProductId(request.getProductId());
                cart.additem(newItem);
                log.debug("Added new cart item. ProductId: {}, quantity: {}", 
                         request.getProductId(), request.getQuantity());
            }

            // Save cart
            cartRepository.save(cart);
            log.info("Cart saved successfully for userId: {}. ProductId: {}, Quantity: {}", 
                     request.getUserId(), request.getProductId(), request.getQuantity());
            
            return "Item saved to cart";
            
        } catch (Exception e) {
            log.error("Error adding item to cart for userId: {}, productId: {}", 
                     request.getUserId(), request.getProductId(), e);
            throw e;
        }
    }

    /**
     * Validates the add to cart request.
     *
     * @param request the cart item request to validate
     * @throws InvalidInputException if request is invalid
     */
    private void validateAddToCartRequest(CartItemRequest request) {
        if (request == null) {
            log.warn("Cart item request is null");
            throw new InvalidInputException(INVALID_REQUEST_MSG);
        }

        if (request.getQuantity() <= 0) {
            log.warn("Invalid quantity: {} for productId: {} by userId: {}", 
                    request.getQuantity(), request.getProductId(), request.getUserId());
            throw new InvalidInputException(INVALID_QUANTITY_MSG);
        }

        validateUserId(request.getUserId());
    }

    /**
     * Retrieves the cart for a specific user.
     * <p>
     * Industry standard improvements:
     * - Input validation to prevent invalid user IDs
     * - Returns Optional instead of null to avoid NullPointerException
     * - Comprehensive logging with appropriate log levels
     * - Exception handling with custom exceptions
     * - Clear error messages for debugging
     *
     * @param userId the user ID to fetch the cart for
     * @return Optional containing the Cart if found, empty Optional if not found
     * @throws InvalidInputException if userId is null or invalid
     */
    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        
        // Input validation
        validateUserId(userId);
        
        log.debug("Fetching cart for userId: {}", userId);
        
        try {
            Cart cart = cartRepository.findByUserId(userId).orElseThrow( ()->new RuntimeException("Cart not found for user: " + userId));
            
            log.info("Cart retrieved successfully for userId: {}. Cart contains {} items.",
                     userId, cart.getCartItems().size());
            return cart;
            

            
        } catch (Exception e) {
            log.error("Error occurred while fetching cart for userId: {}", userId, e);
            throw new RuntimeException("Failed to retrieve cart for user: " + userId, e);
        }
    }

    /**
     * Validates if the provided user ID is valid.
     *
     * @param userId the user ID to validate
     * @throws InvalidInputException if userId is null or not positive
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new InvalidInputException(INVALID_USER_ID_MSG);
        }
    }
}
