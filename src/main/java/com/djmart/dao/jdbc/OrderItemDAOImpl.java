package com.djmart.dao.jdbc;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dto.OrderItemResponse;
import com.djmart.exception.DatabaseException;
import com.djmart.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of OrderItemDAO with joined product details.
 */
public class OrderItemDAOImpl extends BaseDAO implements OrderItemDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderItemDAOImpl.class);

    private static final String SQL_INSERT =
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price, created_at) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_FIND_BY_ORDER_JOIN_PRODUCT =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.created_at, " +
            "       p.name AS product_name, p.image_url " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE oi.order_id = ? " +
            "ORDER BY oi.id ASC";

    private static final String SQL_FIND_BY_ORDER_AND_SELLER =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.created_at, " +
            "       p.name AS product_name, p.image_url " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE oi.order_id = ? AND p.seller_id = ? " +
            "ORDER BY oi.id ASC";

    @Override
    public OrderItem create(Connection conn, OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("OrderItem cannot be null");
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
            return item;
        } catch (SQLException e) {
            LOGGER.error("Failed to insert order item: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to persist order item", e);
        }
    }

    @Override
    public void createBatch(Connection conn, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : items) {
                ps.setLong(1, item.getOrderId());
                ps.setLong(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.error("Failed to insert batch of order items: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to persist order items batch", e);
        }
    }

    @Override
    public List<OrderItemResponse> findByOrderId(Long orderId) {
        List<OrderItemResponse> responses = new ArrayList<>();
        if (orderId == null) {
            return responses;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ORDER_JOIN_PRODUCT)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemResponse res = new OrderItemResponse(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("image_url"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );
                    responses.add(res);
                }
            }
            return responses;
        } catch (SQLException e) {
            LOGGER.error("Failed to find order items for order {}: {}", orderId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve order items", e);
        }
    }

    @Override
    public List<OrderItemResponse> findByOrderIdAndSellerId(Long orderId, Long sellerId) {
        List<OrderItemResponse> responses = new ArrayList<>();
        if (orderId == null || sellerId == null) {
            return responses;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ORDER_AND_SELLER)) {
            ps.setLong(1, orderId);
            ps.setLong(2, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemResponse res = new OrderItemResponse(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("image_url"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );
                    responses.add(res);
                }
            }
            return responses;
        } catch (SQLException e) {
            LOGGER.error("Failed to find seller order items for order {} and seller {}: {}", orderId, sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve seller order items", e);
        }
    }

    @Override
    public java.math.BigDecimal calculateSellerRevenue(Long sellerId) {
        if (sellerId == null) {
            return java.math.BigDecimal.ZERO;
        }
        String sql = "SELECT COALESCE(SUM(oi.unit_price * oi.quantity), 0) " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "WHERE p.seller_id = ? AND o.status != 'CANCELLED'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal rev = rs.getBigDecimal(1);
                    return rev != null ? rev : java.math.BigDecimal.ZERO;
                }
            }
            return java.math.BigDecimal.ZERO;
        } catch (SQLException e) {
            LOGGER.error("Failed to calculate seller revenue for seller {}: {}", sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to calculate seller revenue", e);
        }
    }
}
