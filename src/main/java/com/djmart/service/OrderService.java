package com.djmart.service;

import com.djmart.dto.CheckoutRequest;
import com.djmart.dto.OrderResponse;
import com.djmart.dto.PageResponse;
import com.djmart.model.OrderStatus;
import com.djmart.model.Role;

/**
 * Service interface managing checkout transactions, order fulfillment, and role-based order views.
 */
public interface OrderService {

    /**
     * Executes atomic multi-table checkout transaction:
     * 1. Load cart
     * 2. Validate cart not empty
     * 3. Reload current product prices
     * 4. Validate current stock
     * 5. Calculate total on server
     * 6. Create order
     * 7. Create order items
     * 8. Reduce stock
     * 9. Clear cart
     * 10. Commit (or rollback on failure)
     */
    OrderResponse checkout(Long buyerId, CheckoutRequest request);

    /**
     * Retrieves an order by ID with role-based access control.
     */
    OrderResponse getOrderById(Long orderId, Long requesterId, Role requesterRole);

    /**
     * Retrieves orders placed by a specific buyer.
     */
    PageResponse<OrderResponse> getOrdersByBuyer(Long buyerId, int page, int size);

    /**
     * Retrieves incoming orders containing products listed by a specific seller.
     */
    PageResponse<OrderResponse> getOrdersBySeller(Long sellerId, int page, int size);

    /**
     * Retrieves all marketplace orders (for admin monitoring).
     */
    PageResponse<OrderResponse> getAllOrders(int page, int size);

    /**
     * Updates an order's status following valid lifecycle workflow transitions.
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long updaterId, Role updaterRole);

    /**
     * Cancels an order and restores product inventory if status allows cancellation.
     */
    boolean cancelOrder(Long orderId, Long requesterId, Role requesterRole);

    /**
     * Returns best selling products for analytics.
     */
    java.util.List<java.util.Map<String, Object>> getBestSellingProducts(int limit);

    /**
     * Returns daily sales aggregates for analytics.
     */
    java.util.List<java.util.Map<String, Object>> getDailySales(int days);

    /**
     * Returns platform sales summary for analytics.
     */
    java.util.Map<String, Object> getSalesAnalytics();
}
