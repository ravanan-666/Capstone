package com.djmart.dao;

import com.djmart.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Cart items.
 */
public interface CartDAO {

    /**
     * Adds an item to the user's cart. If the item already exists, updates the quantity.
     */
    CartItem addOrUpdateItem(Long userId, Long productId, int quantity);

    /**
     * Updates the quantity of a specific cart item by ID.
     */
    boolean updateQuantity(Long cartItemId, int newQuantity);

    /**
     * Removes an item from the cart by its cart item ID.
     */
    boolean remove(Long cartItemId);

    /**
     * Removes an item from the cart by user ID and product ID.
     */
    boolean removeByUserAndProduct(Long userId, Long productId);

    /**
     * Clears all items in a user's cart.
     */
    boolean clear(Long userId);

    /**
     * Clears all items in a user's cart within an active database transaction.
     */
    boolean clear(Connection conn, Long userId);

    /**
     * Retrieves all cart items for a user, joined with product catalog information.
     * Prevents N+1 database queries.
     */
    List<CartItem> findByUser(Long userId);

    /**
     * Finds a single cart item by user ID and product ID.
     */
    Optional<CartItem> findByUserAndProduct(Long userId, Long productId);

    /**
     * Finds a single cart item by ID.
     */
    Optional<CartItem> findById(Long cartItemId);
}
