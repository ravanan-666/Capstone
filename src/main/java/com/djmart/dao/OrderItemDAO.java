package com.djmart.dao;

import com.djmart.dto.OrderItemResponse;
import com.djmart.model.OrderItem;

import java.sql.Connection;
import java.util.List;

/**
 * Data Access Object interface for OrderItem line entities.
 */
public interface OrderItemDAO {

    /**
     * Persists an order item within an active transaction.
     */
    OrderItem create(Connection conn, OrderItem item);

    /**
     * Persists multiple order items in a batch within an active transaction.
     */
    void createBatch(Connection conn, List<OrderItem> items);

    /**
     * Finds order items for an order, joined with product details to avoid N+1 queries.
     */
    List<OrderItemResponse> findByOrderId(Long orderId);

    /**
     * Finds order items belonging to a specific seller within an order.
     */
    List<OrderItemResponse> findByOrderIdAndSellerId(Long orderId, Long sellerId);
}
