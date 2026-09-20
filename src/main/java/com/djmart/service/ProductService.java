package com.djmart.service;

import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface managing product catalog, seller listings, search, and inventory rules.
 */
public interface ProductService {

    /**
     * Creates a new product listing on behalf of a seller.
     */
    ProductResponse createProduct(Long sellerId, ProductRequest request);

    /**
     * Updates an existing product listing. Enforces seller ownership unless isAdmin is true.
     */
    ProductResponse updateProduct(Long sellerId, Long productId, ProductRequest request, boolean isAdmin);

    /**
     * Deletes a product listing. Enforces seller ownership unless isAdmin is true.
     */
    boolean deleteProduct(Long sellerId, Long productId, boolean isAdmin);

    /**
     * Retrieves product details including aggregated average star rating and review count.
     */
    ProductResponse getProductById(Long productId);

    /**
     * Searches and filters products with validated pagination, price filtering, and sorting.
     */
    PageResponse<ProductResponse> searchProducts(String keyword, String category,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 String sortBy, String sortOrder,
                                                 int page, int size);

    /**
     * Retrieves products listed by a specific seller.
     */
    PageResponse<ProductResponse> getProductsBySeller(Long sellerId, int page, int size);

    /**
     * Returns all distinct category names currently present in the marketplace catalog.
     */
    List<String> getCategories();
}
