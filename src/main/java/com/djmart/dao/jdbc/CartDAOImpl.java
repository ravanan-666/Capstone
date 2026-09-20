package com.djmart.dao.jdbc;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.CartDAO;
import com.djmart.exception.DatabaseException;
import com.djmart.model.CartItem;
import com.djmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CartDAO with joined product details to prevent N+1 queries.
 */
public class CartDAOImpl extends BaseDAO implements CartDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartDAOImpl.class);

    private static final String SQL_FIND_BY_USER_JOIN_PRODUCT =
            "SELECT c.id AS cart_id, c.user_id, c.product_id, c.quantity, c.created_at AS cart_created_at, " +
            "       p.seller_id, p.name AS product_name, p.description, p.price, p.stock_qty, p.category, p.image_url, p.created_at AS product_created_at " +
            "FROM cart_items c " +
            "JOIN products p ON c.product_id = p.id " +
            "WHERE c.user_id = ? " +
            "ORDER BY c.created_at DESC";

    private static final String SQL_FIND_BY_USER_AND_PRODUCT =
            "SELECT id, user_id, product_id, quantity, created_at FROM cart_items WHERE user_id = ? AND product_id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, user_id, product_id, quantity, created_at FROM cart_items WHERE id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO cart_items (user_id, product_id, quantity, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_UPDATE_QTY =
            "UPDATE cart_items SET quantity = ? WHERE id = ?";

    private static final String SQL_DELETE_BY_ID =
            "DELETE FROM cart_items WHERE id = ?";

    private static final String SQL_DELETE_BY_USER_AND_PRODUCT =
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

    private static final String SQL_CLEAR_BY_USER =
            "DELETE FROM cart_items WHERE user_id = ?";

    @Override
    public CartItem addOrUpdateItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid user ID, product ID, or quantity");
        }

        Optional<CartItem> existing = findByUserAndProduct(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            updateQuantity(item.getId(), newQty);
            item.setQuantity(newQty);
            return item;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findById(id).orElse(new CartItem(id, userId, productId, quantity, null));
                }
            }
            throw new DatabaseException("Failed to obtain generated ID for cart item");
        } catch (SQLException e) {
            LOGGER.error("Failed to add cart item for user {} and product {}: {}", userId, productId, e.getMessage(), e);
            throw new DatabaseException("Failed to add item to cart", e);
        }
    }

    @Override
    public boolean updateQuantity(Long cartItemId, int newQuantity) {
        if (cartItemId == null || newQuantity <= 0) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_QTY)) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update quantity for cart item {}: {}", cartItemId, e.getMessage(), e);
            throw new DatabaseException("Failed to update cart quantity", e);
        }
    }

    @Override
    public boolean remove(Long cartItemId) {
        if (cartItemId == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BY_ID)) {
            ps.setLong(1, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to remove cart item {}: {}", cartItemId, e.getMessage(), e);
            throw new DatabaseException("Failed to remove cart item", e);
        }
    }

    @Override
    public boolean removeByUserAndProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BY_USER_AND_PRODUCT)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to remove item by user {} and product {}: {}", userId, productId, e.getMessage(), e);
            throw new DatabaseException("Failed to remove cart item", e);
        }
    }

    @Override
    public boolean clear(Long userId) {
        try (Connection conn = getConnection()) {
            return clear(conn, userId);
        } catch (SQLException e) {
            LOGGER.error("Failed to clear cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseException("Failed to clear user cart", e);
        }
    }

    @Override
    public boolean clear(Connection conn, Long userId) {
        if (userId == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_CLEAR_BY_USER)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Failed to clear cart in transaction for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseException("Failed to clear cart in transaction", e);
        }
    }

    @Override
    public List<CartItem> findByUser(Long userId) {
        List<CartItem> items = new ArrayList<>();
        if (userId == null) {
            return items;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_JOIN_PRODUCT)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("cart_id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("cart_created_at"));

                    Product product = new Product();
                    product.setId(rs.getLong("product_id"));
                    product.setSellerId(rs.getLong("seller_id"));
                    product.setName(rs.getString("product_name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setStockQty(rs.getInt("stock_qty"));
                    product.setCategory(rs.getString("category"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setCreatedAt(rs.getTimestamp("product_created_at"));

                    item.setProduct(product);
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException e) {
            LOGGER.error("Failed to load cart for user {}: {}", userId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve cart items", e);
        }
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_AND_PRODUCT)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to check cart item: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to check cart item", e);
        }
    }

    @Override
    public Optional<CartItem> findById(Long cartItemId) {
        if (cartItemId == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, cartItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find cart item by ID {}: {}", cartItemId, e.getMessage(), e);
            throw new DatabaseException("Failed to find cart item by ID", e);
        }
    }
}
