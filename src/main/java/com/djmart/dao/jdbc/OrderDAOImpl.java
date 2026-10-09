package com.djmart.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.OrderDAO;
import com.djmart.exception.DatabaseException;
import com.djmart.model.Order;
import com.djmart.model.OrderStatus;

/**
 * JDBC implementation of OrderDAO with transaction support and seller filtering.
 */
public class OrderDAOImpl extends BaseDAO implements OrderDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDAOImpl.class);

    private static final String SELECT_COLUMNS =
            "id, buyer_id, status, total_amount, shipping_address, created_at";

    private static final String SQL_INSERT =
            "INSERT INTO orders (buyer_id, status, total_amount, shipping_address, created_at) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_FIND_BY_ID =
            "SELECT " + SELECT_COLUMNS + " FROM orders WHERE id = ?";

    private static final String SQL_FIND_BY_BUYER =
            "SELECT " + SELECT_COLUMNS + " FROM orders WHERE buyer_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_BY_BUYER =
            "SELECT COUNT(*) FROM orders WHERE buyer_id = ?";

    private static final String SQL_FIND_RELEVANT_SELLER_ORDERS =
            "SELECT DISTINCT o.id, o.buyer_id, o.status, o.total_amount, o.shipping_address, o.created_at " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE p.seller_id = ? " +
            "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_RELEVANT_SELLER_ORDERS =
            "SELECT COUNT(DISTINCT o.id) " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "WHERE p.seller_id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE orders SET status = ? WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT " + SELECT_COLUMNS + " FROM orders ORDER BY created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) FROM orders";

    @Override
    public Order create(Connection conn, Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setString(2, order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getShippingAddress());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating order failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    order.setId(rs.getLong(1));
                }
            }
            return order;
        } catch (SQLException e) {
            LOGGER.error("Failed to insert order: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to persist order", e);
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOrder(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find order by ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve order by ID", e);
        }
    }

    @Override
    public List<Order> findByBuyer(Long buyerId, int offset, int limit) {
        List<Order> orders = new ArrayList<>();
        if (buyerId == null) {
            return orders;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_BUYER)) {
            ps.setLong(1, buyerId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
            return orders;
        } catch (SQLException e) {
            LOGGER.error("Failed to find orders for buyer {}: {}", buyerId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve buyer orders", e);
        }
    }

    @Override
    public long countByBuyer(Long buyerId) {
        if (buyerId == null) {
            return 0;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_BUYER)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count orders for buyer {}: {}", buyerId, e.getMessage(), e);
            throw new DatabaseException("Failed to count buyer orders", e);
        }
    }

    @Override
    public List<Order> findRelevantSellerOrders(Long sellerId, int offset, int limit) {
        List<Order> orders = new ArrayList<>();
        if (sellerId == null) {
            return orders;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_RELEVANT_SELLER_ORDERS)) {
            ps.setLong(1, sellerId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
            return orders;
        } catch (SQLException e) {
            LOGGER.error("Failed to find incoming orders for seller {}: {}", sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve seller orders", e);
        }
    }

    @Override
    public long countRelevantSellerOrders(Long sellerId) {
        if (sellerId == null) {
            return 0;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_RELEVANT_SELLER_ORDERS)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count incoming orders for seller {}: {}", sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to count seller orders", e);
        }
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus newStatus) {
        try (Connection conn = getConnection()) {
            return updateStatus(conn, orderId, newStatus);
        } catch (SQLException e) {
            LOGGER.error("Failed to update status for order {}: {}", orderId, e.getMessage(), e);
            throw new DatabaseException("Failed to update order status", e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Long orderId, OrderStatus newStatus) {
        if (orderId == null || newStatus == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update status for order {} in transaction: {}", orderId, e.getMessage(), e);
            throw new DatabaseException("Failed to update order status", e);
        }
    }

    @Override
    public boolean cancel(Long orderId) {
        return updateStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    public List<Order> findAll(int offset, int limit) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL)) {
            ps.setInt(1, Math.max(1, limit));
            ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
            return orders;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch all orders: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve orders", e);
        }
    }

    @Override
    public long countAll() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count all orders: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to count orders", e);
        }
    }

    @Override
    public java.math.BigDecimal calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status != 'CANCELLED'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                java.math.BigDecimal rev = rs.getBigDecimal(1);
                return rev != null ? rev : java.math.BigDecimal.ZERO;
            }
            return java.math.BigDecimal.ZERO;
        } catch (SQLException e) {
            LOGGER.error("Failed to calculate total revenue: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to calculate total revenue", e);
        }
    }

    @Override
    public List<java.util.Map<String, Object>> getBestSellingProducts(int limit) {
        String sql = "SELECT p.id, p.name, p.category, p.price, p.image_url, " +
                     "SUM(oi.quantity) as total_units_sold, " +
                     "SUM(oi.quantity * oi.unit_price) as total_revenue " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE o.status != 'CANCELLED' " +
                     "GROUP BY p.id, p.name, p.category, p.price, p.image_url " +
                     "ORDER BY total_units_sold DESC LIMIT ?";
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> map = new HashMap<>();
                    map.put("productId", rs.getLong("id"));
                    map.put("name", rs.getString("name"));
                    map.put("category", rs.getString("category"));
                    map.put("price", rs.getBigDecimal("price"));
                    map.put("imageUrl", rs.getString("image_url"));
                    map.put("unitsSold", rs.getInt("total_units_sold"));
                    map.put("revenue", rs.getBigDecimal("total_revenue"));
                    list.add(map);
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch best selling products: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch best selling products", e);
        }
    }

    @Override
    public List<java.util.Map<String, Object>> getDailySales(int days) {
        String sql = "SELECT CAST(created_at AS DATE) as order_date, " +
                     "COUNT(*) as order_count, " +
                     "SUM(total_amount) as total_revenue " +
                     "FROM orders " +
                     "WHERE status != 'CANCELLED' " +
                     "GROUP BY CAST(created_at AS DATE) " +
                     "ORDER BY order_date DESC LIMIT ?";
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, days));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> map = new HashMap<>();
                    map.put("date", rs.getString("order_date"));
                    map.put("orderCount", rs.getLong("order_count"));
                    map.put("revenue", rs.getBigDecimal("total_revenue"));
                    list.add(map);
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch daily sales: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch daily sales", e);
        }
    }

    @Override
    public java.util.Map<String, Object> getSalesSummary() {
        java.util.Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) as total_orders, " +
                     "COALESCE(SUM(CASE WHEN status != 'CANCELLED' THEN total_amount ELSE 0 END), 0) as valid_revenue, " +
                     "COUNT(CASE WHEN status = 'DELIVERED' THEN 1 END) as delivered_orders, " +
                     "COUNT(CASE WHEN status = 'PENDING' OR status = 'CONFIRMED' THEN 1 END) as active_orders, " +
                     "COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders " +
                     "FROM orders";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                map.put("totalOrders", rs.getLong("total_orders"));
                map.put("validRevenue", rs.getBigDecimal("valid_revenue"));
                map.put("deliveredOrders", rs.getLong("delivered_orders"));
                map.put("activeOrders", rs.getLong("active_orders"));
                map.put("cancelledOrders", rs.getLong("cancelled_orders"));
            }
            return map;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch sales summary: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch sales summary", e);
        }
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        return order;
    }
}
