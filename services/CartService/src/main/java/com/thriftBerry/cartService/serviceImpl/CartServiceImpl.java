package com.thriftBerry.cartService.serviceImpl;

import com.thriftBerry.cartService.communication.InventoryClient;
import com.thriftBerry.cartService.communication.ProductClient;
import com.thriftBerry.cartService.dto.AvailabilityResponse;
import com.thriftBerry.cartService.dto.CartItemRequest;
import com.thriftBerry.cartService.dto.ProductDto;
import com.thriftBerry.cartService.exceptions.CartFetchException;
import com.thriftBerry.cartService.exceptions.CartNotFoundException;
import com.thriftBerry.cartService.exceptions.InvalidInputException;
import com.thriftBerry.cartService.exceptions.ProductNotFoundException;
import com.thriftBerry.cartService.model.Cart;
import com.thriftBerry.cartService.model.CartItem;
import com.thriftBerry.cartService.repository.CartRepository;
import com.thriftBerry.cartService.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    
    // Constants for error messages
    private static final String INVALID_QUANTITY_MSG = "Quantity must be at least one";
    private static final String INSUFFICIENT_STOCK_MSG = "Insufficient stock: Available Quantity: %d";
    private static final String INVALID_USER_ID_MSG = "User ID must be positive";
    private static final String INVALID_REQUEST_MSG = "Cart item request cannot be null";
    private static final String INVALID_PRODUCT_ID_MSG = "Product ID must be positive";
    private static final String PRODUCT_REMOVED_MSG = "Product removed successfully from cart";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found in cart";
    private static final String CART_CLEARED_MSG = "Cart cleared successfully";
    private static final String CART_ALREADY_EMPTY_MSG = "Cart is already empty for user";

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
        
        log.debug("Processing add to cart request: userId={}, productId={}, quantity={} ",
                  request.getUserId(), request.getProductId(), request.getQuantity());

         verifyProductExists(request.getProductId());
         ProductDto productDto = productClient.getProductById(request.getProductId());
        BigDecimal price = productDto.getPrice();
         log.debug("Product details retrieved for productId: {}. Product Name: {}, Price: {}",
                  request.getProductId(), productDto.getProductName(), productDto.getPrice());
        checkInventoryAvailability(request.getProductId(), request.getQuantity());

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
                newItem.setPrice(price);
                cart.additem(newItem);
                log.debug("Added new cart item. ProductId: {}, quantity: {}, price: {}",
                         request.getProductId(), request.getQuantity(),price);
            }

            // Save cart
            cartRepository.save(cart);
            log.info("Cart saved successfully for userId: {}. ProductId: {}, Quantity: {}, price: {}",
                     request.getUserId(), request.getProductId(), request.getQuantity(),price);
            
            return "Item saved to cart";
            
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


    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        
        // Input validation
        validateUserId(userId);
        
        log.debug("Fetching cart for userId: {}", userId);
        
        try {
            Cart cart = cartRepository.findByUserId(userId).orElseThrow( ()->new CartNotFoundException("Cart not found for user: " + userId));
            
            log.info("Cart retrieved successfully for userId: {}. Cart contains {} items.",
                     userId, cart.getCartItems().size());
            return cart;
            

            
        } catch (Exception e) {
            log.error("Error occurred while fetching cart for userId: {}", userId, e);
            throw new CartFetchException("Failed to retrieve cart for user: " + userId, e);
        }
    }


    @Override
    @Transactional
    public Cart updateCartItem(CartItemRequest cartItemRequest) {
        
        // Input validation
        validateAddToCartRequest(cartItemRequest);
        
        log.debug("Processing update cart item request: userId={}, productId={}, quantity={}",
                  cartItemRequest.getUserId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());

        try {
            // Verify product exists and check inventory before updating cart item
            verifyProductExists(cartItemRequest.getProductId());
            checkInventoryAvailability(cartItemRequest.getProductId(), cartItemRequest.getQuantity());

            // Get cart for user
            Cart cart = getCartByUserId(cartItemRequest.getUserId());

            // Find item in cart
            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getProductId().equals(cartItemRequest.getProductId()))
                    .findFirst();

            // If item not found in cart, throw exception
            if (existingItem.isEmpty()) {
                log.warn("Product not found in cart for update. UserId: {}, ProductId: {}",
                         cartItemRequest.getUserId(), cartItemRequest.getProductId());
                throw new ProductNotFoundException("Product with ID: " + cartItemRequest.getProductId() +
                                         " not found in cart for user: " + cartItemRequest.getUserId());
            }

            // Update the item quantity
            CartItem item = existingItem.get();
            long previousQuantity = item.getQuantity();
            item.setQuantity(cartItemRequest.getQuantity());

            
            log.debug("Updated cart item. ProductId: {}, previous quantity: {}, new quantity: {}",
                     cartItemRequest.getProductId(), previousQuantity, cartItemRequest.getQuantity());

            // Save and return updated cart
            Cart updatedCart = cartRepository.save(cart);
            
            log.info("Cart item updated successfully for userId: {}. ProductId: {}, Quantity: {}",
                     cartItemRequest.getUserId(), cartItemRequest.getProductId(), cartItemRequest.getQuantity());
            
            return updatedCart;
            
        } catch (InvalidInputException e) {
            log.error("Validation error while updating cart item. UserId: {}, ProductId: {}, Error: {}",
                     cartItemRequest.getUserId(), cartItemRequest.getProductId(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error occurred while updating cart item. UserId: {}, ProductId: {}",
                     cartItemRequest.getUserId(), cartItemRequest.getProductId(), e);
            throw e;
        }
    }

    /**
     * Removes a product from the user's cart.
     * <p>
     * Industry standard improvements:
     * - Input validation for both userId and productId
     * - Transactional handling for data consistency
     * - Comprehensive logging with appropriate log levels
     * - Null safety checks
     * - Proper exception handling
     * - Clear success/failure messages
     *
     * @param userId     the user ID whose cart to modify
     * @param productId  the product ID to remove from cart
     * @return success message if product was removed, failure message otherwise
     * @throws InvalidInputException if userId or productId is invalid
     * @throws RuntimeException      if cart not found or removal fails
     */
    @Override
    @Transactional
    public String removeProduct(Long userId, Long productId) {
        
        // Input validation
        validateUserId(userId);
        validateProductId(productId);
        
        log.debug("Attempting to remove product from cart. UserId: {}, ProductId: {}", userId, productId);
        
        try {
            // Get or throw exception if cart not found
            Cart cart = getCartByUserIdOrThrow(userId);
            
            // Validate cart is not empty
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.warn("Cart is empty for userId: {}. Cannot remove productId: {}", userId, productId);
                return PRODUCT_NOT_FOUND_MSG;
            }
            
            // Remove product from cart
            boolean removed = cart.getCartItems().removeIf(item -> 
                Objects.equals(item.getProductId(), productId)
            );
            
            if (removed) {
                // Save cart after modification
                saveCart(cart);
                log.info("Product removed successfully from cart. UserId: {}, ProductId: {}, Remaining items: {}",
                        userId, productId, cart.getCartItems().size());
                return PRODUCT_REMOVED_MSG;
            } else {
                log.debug("Product not found in cart for removal. UserId: {}, ProductId: {}", userId, productId);
                return PRODUCT_NOT_FOUND_MSG;
            }
            
        } catch (InvalidInputException e) {
            log.error("Validation error while removing product. UserId: {}, ProductId: {}, Error: {}",
                     userId, productId, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error occurred while removing product from cart. UserId: {}, ProductId: {}",
                     userId, productId, e);
            throw new RuntimeException("Failed to remove product from cart: " + e.getMessage(), e);
        }
    }

    /**
     * Clears all items from the user's cart.
     * <p>
     * Industry standard improvements:
     * - Input validation to ensure valid userId
     * - Transactional handling for data consistency
     * - Null safety checks before clearing cart items
     * - Validation that cart exists before clearing
     * - Comprehensive logging with appropriate log levels
     * - Detects if cart is already empty and logs accordingly
     * - Proper exception handling with context
     * - Uses constants for return messages
     * - Returns meaningful message to caller
     *
     * @param userId the user ID whose cart to clear
     * @return success message if cart was cleared, info message if already empty
     * @throws InvalidInputException if userId is invalid
     * @throws RuntimeException      if cart not found or clearing fails
     */
    @Override
    @Transactional
    public String clearCart(Long userId) {
        
        // Input validation
        validateUserId(userId);
        
        log.debug("Attempting to clear cart for userId: {}", userId);
        
        try {
            // Get cart or throw exception if not found
            Cart cart = getCartByUserIdOrThrow(userId);
            
            // Check if cart items exist and are not empty
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.info("Cart is already empty for userId: {}", userId);
                return CART_ALREADY_EMPTY_MSG;
            }
            
            // Get count of items before clearing for logging
            int itemCount = cart.getCartItems().size();
            
            // Clear all items from cart
            cart.getCartItems().clear();
            saveCart(cart);
            
            log.info("Cart cleared successfully for userId: {}. Removed {} items from cart",
                     userId, itemCount);
            
            return CART_CLEARED_MSG;
            
        } catch (InvalidInputException e) {
            log.error("Validation error while clearing cart. UserId: {}, Error: {}", userId, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error occurred while clearing cart for userId: {}", userId, e);
            throw new RuntimeException("Failed to clear cart for user: " + userId, e);
        }
    }

    private void saveCart(Cart cart){
        cartRepository.save(cart);
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

    /**
     * Validates if the provided product ID is valid.
     *
     * @param productId the product ID to validate
     * @throws InvalidInputException if productId is null or not positive
     */
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            log.warn("Invalid productId provided: {}", productId);
            throw new InvalidInputException(INVALID_PRODUCT_ID_MSG);
        }
    }


    private void verifyProductExists(Long productId) {
        try {
            productClient.getProductById(productId);
            log.debug("Product verification successful for productId: {}", productId);
        } catch (Exception e) {
            log.error("Product verification failed for productId: {}", productId, e);
            throw new RuntimeException("Product not found with ID: " + productId, e);
        }
    }

    private void checkInventoryAvailability(Long productId, Long quantity) {
        try {
            AvailabilityResponse response = inventoryClient.checkAvailability(productId, quantity);
            if (Boolean.FALSE.equals(response.getAvailable())) {
                log.warn("Insufficient stock for productId: {} requested quantity: {}. Available: {}",
                        productId, quantity, response.getAvailableQuantity());
                throw new RuntimeException(String.format(INSUFFICIENT_STOCK_MSG, response.getAvailableQuantity()));
            }
            log.debug("Stock verified for productId: {}. Requested quantity: {}, Available: {}",
                     productId, quantity, response.getAvailableQuantity());
        } catch (Exception e) {
            log.error("Inventory check failed for productId: {}", productId, e);
            throw new RuntimeException("Failed to check inventory for product ID: " + productId, e);
        }
    }


    private Cart getCartByUserIdOrThrow(Long userId){
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
    }
}
