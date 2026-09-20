package com.djmart.dao.jdbc;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.exception.DatabaseException;
import com.djmart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ProductDAO.
 * Strictly uses PreparedStatements, handles pagination and safe dynamic querying.
 */
public class ProductDAOImpl extends BaseDAO implements ProductDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDAOImpl.class);

    private static final String SELECT_COLUMNS =
            "id, seller_id, name, description, price, stock_qty, category, image_url, created_at";

    private static final String SQL_FIND_BY_ID =
            "SELECT " + SELECT_COLUMNS + " FROM products WHERE id = ?";

    private static final String SQL_LOCK_FOR_UPDATE =
            "SELECT " + SELECT_COLUMNS + " FROM products WHERE id = ? FOR UPDATE";

    private static final String SQL_INSERT =
            "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_UPDATE =
            "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ? " +
            "WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM products WHERE id = ?";

    private static final String SQL_FIND_BY_SELLER =
            "SELECT " + SELECT_COLUMNS + " FROM products WHERE seller_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_BY_SELLER =
            "SELECT COUNT(*) FROM products WHERE seller_id = ?";

    private static final String SQL_FIND_BY_CATEGORY =
            "SELECT " + SELECT_COLUMNS + " FROM products WHERE category = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_DECREMENT_STOCK =
            "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";

    private static final String SQL_UPDATE_STOCK =
            "UPDATE products SET stock_qty = ? WHERE id = ?";

    private static final String SQL_DISTINCT_CATEGORIES =
            "SELECT DISTINCT category FROM products ORDER BY category ASC";

    @Override
    public Product create(Product product) {
        try (Connection conn = getConnection()) {
            return create(conn, product);
        } catch (SQLException e) {
            LOGGER.error("Failed to create product {}: {}", product.getName(), e.getMessage(), e);
            throw new DatabaseException("Failed to create product", e);
        }
    }

    @Override
    public Product create(Connection conn, Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty() != null ? product.getStockQty() : 0);
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                }
            }
            return findById(product.getId()).orElse(product);
        } catch (SQLException e) {
            LOGGER.error("Failed to persist product {}: {}", product.getName(), e.getMessage(), e);
            throw new DatabaseException("Failed to persist product", e);
        }
    }

    @Override
    public boolean update(Product product) {
        if (product == null || product.getId() == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update product {}: {}", product.getId(), e.getMessage(), e);
            throw new DatabaseException("Failed to update product", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to delete product {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to delete product", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find product by id {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to find product by ID", e);
        }
    }

    @Override
    public Optional<Product> lockProductForUpdate(Connection conn, Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_LOCK_FOR_UPDATE)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to lock product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to lock product for update", e);
        }
    }

    @Override
    public List<Product> findBySeller(Long sellerId, int offset, int limit) {
        List<Product> products = new ArrayList<>();
        if (sellerId == null) {
            return products;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SELLER)) {
            ps.setLong(1, sellerId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
            return products;
        } catch (SQLException e) {
            LOGGER.error("Failed to find products by seller {}: {}", sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to find products by seller", e);
        }
    }

    @Override
    public long countBySeller(Long sellerId) {
        if (sellerId == null) {
            return 0;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_SELLER)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count products by seller {}: {}", sellerId, e.getMessage(), e);
            throw new DatabaseException("Failed to count products by seller", e);
        }
    }

    @Override
    public List<Product> findByCategory(String category, int offset, int limit) {
        List<Product> products = new ArrayList<>();
        if (category == null || category.trim().isEmpty()) {
            return products;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CATEGORY)) {
            ps.setString(1, category.trim());
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
            return products;
        } catch (SQLException e) {
            LOGGER.error("Failed to find products by category {}: {}", category, e.getMessage(), e);
            throw new DatabaseException("Failed to find products by category", e);
        }
    }

    @Override
    public List<Product> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                String sortBy, String sortOrder, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLUMNS + " FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        buildFilterClauses(sql, params, keyword, category, minPrice, maxPrice);

        // Safe Sort By & Order (Strict Whitelist)
        String sortCol = sanitizeSortColumn(sortBy);
        String sortDir = sanitizeSortDirection(sortOrder);
        sql.append(" ORDER BY ").append(sortCol).append(" ").append(sortDir);

        sql.append(" LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
            return products;
        } catch (SQLException e) {
            LOGGER.error("Failed to search products: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to execute product search query", e);
        }
    }

    @Override
    public long countSearch(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        buildFilterClauses(sql, params, keyword, category, minPrice, maxPrice);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count product search results: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to count product search results", e);
        }
    }

    @Override
    public boolean decrementStock(Connection conn, Long productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_DECREMENT_STOCK)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setInt(3, quantity); // Ensure stock_qty >= quantity
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to decrement stock for product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to decrement product inventory", e);
        }
    }

    @Override
    public boolean updateStock(Long productId, int newStock) {
        if (productId == null || newStock < 0) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STOCK)) {
            ps.setInt(1, newStock);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update stock for product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to update product stock", e);
        }
    }

    @Override
    public List<String> findDistinctCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DISTINCT_CATEGORIES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            return categories;
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch distinct categories: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch categories", e);
        }
    }

    private void buildFilterClauses(StringBuilder sql, List<Object> params,
                                    String keyword, String category,
                                    BigDecimal minPrice, BigDecimal maxPrice) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?)");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
    }

    private String sanitizeSortColumn(String sortBy) {
        if (sortBy == null) {
            return "created_at";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "price" -> "price";
            case "name" -> "name";
            case "stock", "stock_qty" -> "stock_qty";
            default -> "created_at";
        };
    }

    private String sanitizeSortDirection(String sortOrder) {
        if (sortOrder != null && "asc".equalsIgnoreCase(sortOrder.trim())) {
            return "ASC";
        }
        return "DESC";
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setSellerId(rs.getLong("seller_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQty(rs.getInt("stock_qty"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        return product;
    }
}
