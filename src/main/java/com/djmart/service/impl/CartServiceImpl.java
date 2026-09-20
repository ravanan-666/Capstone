package com.djmart.service.impl;

import com.djmart.dao.CartDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dto.CartItemResponse;
import com.djmart.dto.CartResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.InsufficientStockException;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.exception.ResourceNotFoundException;
import com.djmart.model.CartItem;
import com.djmart.model.Product;
import com.djmart.service.CartService;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business service implementation for shopping cart management and server-side pricing.
 */
public class CartServiceImpl implements CartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public CartResponse getCart(Long userId) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(userId, "userId", errors);
        errors.throwIfHasErrors("Invalid user ID for cart lookup");

        List<CartItem> cartItems = cartDAO.findByUser(userId);
        List<CartItemResponse> responses = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product != null) {
                CartItemResponse itemResponse = new CartItemResponse(
                        item.getId(),
                        product.getId(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice(), // Server authoritative price
                        item.getQuantity()
                );
                responses.add(itemResponse);
            }
        }

        return new CartResponse(responses);
    }

    @Override
    public CartItemResponse addToCart(Long userId, Long productId, int quantity) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(userId, "userId", errors);
        ValidationUtil.validateId(productId, "productId", errors);
        ValidationUtil.validateCartQuantity(quantity, errors);
        errors.throwIfHasErrors("Cart item validation failed");

        // 1. Product must exist in catalog
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 2. Validate requested quantity against current available inventory
        Optional<CartItem> existing = cartDAO.findByUserAndProduct(userId, productId);
        int targetQuantity = quantity + (existing.map(CartItem::getQuantity).orElse(0));

        if (targetQuantity > product.getStockQty()) {
            LOGGER.warn("Add to cart rejected: Product ID {} has stock {}, requested total {}",
                    productId, product.getStockQty(), targetQuantity);
            throw new InsufficientStockException(productId, targetQuantity, product.getStockQty());
        }

        // 3. Persist in database
        CartItem saved = cartDAO.addOrUpdateItem(userId, productId, quantity);
        LOGGER.info("Added product ID {} (qty: {}) to cart for user ID {}", productId, quantity, userId);

        return new CartItemResponse(
                saved.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                targetQuantity
        );
    }

    @Override
    public CartItemResponse updateQuantity(Long userId, Long cartItemId, int newQuantity) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(userId, "userId", errors);
        ValidationUtil.validateId(cartItemId, "cartItemId", errors);
        ValidationUtil.validateCartQuantity(newQuantity, errors);
        errors.throwIfHasErrors("Cart quantity update validation failed");

        CartItem cartItem = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        // Enforce user ownership of cart item
        if (!cartItem.getUserId().equals(userId)) {
            LOGGER.warn("Unauthorized attempt to update cart item {} by user {}", cartItemId, userId);
            throw new AuthorizationException("You are not authorized to modify this cart item");
        }

        Product product = productDAO.findById(cartItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(cartItem.getProductId()));

        if (newQuantity > product.getStockQty()) {
            LOGGER.warn("Cart update rejected: Product ID {} has stock {}, requested {}",
                    product.getId(), product.getStockQty(), newQuantity);
            throw new InsufficientStockException(product.getId(), newQuantity, product.getStockQty());
        }

        cartDAO.updateQuantity(cartItemId, newQuantity);
        LOGGER.info("Updated cart item ID {} quantity to {} for user ID {}", cartItemId, newQuantity, userId);

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                newQuantity
        );
    }

    @Override
    public boolean removeFromCart(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            return false;
        }

        CartItem cartItem = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        if (!cartItem.getUserId().equals(userId)) {
            throw new AuthorizationException("You are not authorized to remove this cart item");
        }

        return cartDAO.remove(cartItemId);
    }

    @Override
    public boolean clearCart(Long userId) {
        if (userId == null) {
            return false;
        }
        return cartDAO.clear(userId);
    }
}
