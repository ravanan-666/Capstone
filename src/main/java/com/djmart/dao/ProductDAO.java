package com.djmart.dao;

import com.djmart.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product entities.
 */
public interface ProductDAO {

    /**
     * Persists a new product listing.
     */
    Product create(Product product);

    /**
     * Persists a new product listing within an existing transaction.
     */
    Product create(Connection conn, Product product);

    /**
     * Updates an existing product's details.
     */
    boolean update(Product product);

    /**
     * Deletes a product by ID.
     */
    boolean delete(Long id);

    /**
     * Finds a single product by ID.
     */
    Optional<Product> findById(Long id);

    /**
     * Locks and retrieves a product row for update within an active transaction.
     */
    Optional<Product> lockProductForUpdate(Connection conn, Long productId);

    /**
     * Returns products listed by a specific seller with pagination.
     */
    List<Product> findBySeller(Long sellerId, int offset, int limit);

    /**
     * Returns total count of products listed by a specific seller.
     */
    long countBySeller(Long sellerId);

    /**
     * Returns products by category with pagination.
     */
    List<Product> findByCategory(String category, int offset, int limit);

    /**
     * Search and filter products dynamically by keyword, category, price range, and sort order.
     */
    List<Product> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice,
                         String sortBy, String sortOrder, int offset, int limit);

    /**
     * Counts total products matching search and filter criteria for pagination calculation.
     */
    long countSearch(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Decrements inventory stock for a product within an active transaction.
     * Fails if remaining stock would be negative.
     */
    boolean decrementStock(Connection conn, Long productId, int quantity);

    /**
     * Updates the stock quantity directly.
     */
    boolean updateStock(Long productId, int newStock);

    /**
     * Returns distinct category names available in the catalog.
     */
    List<String> findDistinctCategories();
}
