package com.djmart.service;

import com.djmart.dto.CartItemResponse;
import com.djmart.dto.CartResponse;

/**
 * Service interface managing shopping cart operations, stock checks, and server-side pricing.
 */
public interface CartService {

    /**
     * Retrieves the buyer's active cart with server-calculated running totals and item subtotals.
     * Never trusts client-supplied prices or sums.
     */
    CartResponse getCart(Long userId);

    /**
     * Adds an item to the buyer's cart after validating product existence and stock availability.
     */
    CartItemResponse addToCart(Long userId, Long productId, int quantity);

    /**
     * Updates an existing cart item's quantity after validating available inventory.
     */
    CartItemResponse updateQuantity(Long userId, Long cartItemId, int newQuantity);

    /**
     * Removes an item from the cart.
     */
    boolean removeFromCart(Long userId, Long cartItemId);

    /**
     * Clears all items from the buyer's cart.
     */
    boolean clearCart(Long userId);
}
