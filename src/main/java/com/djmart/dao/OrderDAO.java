package com.djmart.dao;

import com.djmart.model.Order;
import com.djmart.model.OrderStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Order entities.
 */
public interface OrderDAO {

    /**
     * Persists an order within an active database transaction.
     */
    Order create(Connection conn, Order order);

    /**
     * Finds an order by its primary key ID.
     */
    Optional<Order> findById(Long id);

    /**
     * Finds orders placed by a buyer with pagination.
     */
    List<Order> findByBuyer(Long buyerId, int offset, int limit);

    /**
     * Counts total orders placed by a buyer.
     */
    long countByBuyer(Long buyerId);

    /**
     * Finds incoming orders for a seller (orders containing at least one item sold by the seller).
     */
    List<Order> findRelevantSellerOrders(Long sellerId, int offset, int limit);

    /**
     * Counts incoming orders for a seller.
     */
    long countRelevantSellerOrders(Long sellerId);

    /**
     * Updates an order's fulfillment workflow status.
     */
    boolean updateStatus(Long orderId, OrderStatus newStatus);

    /**
     * Updates an order's status within an active transaction.
     */
    boolean updateStatus(Connection conn, Long orderId, OrderStatus newStatus);

    /**
     * Cancels an order if permitted (sets status to CANCELLED).
     */
    boolean cancel(Long orderId);

    /**
     * Returns a paginated list of all orders across the marketplace (for admin management).
     */
    List<Order> findAll(int offset, int limit);

    /**
     * Counts total marketplace orders.
     */
    long countAll();
}
