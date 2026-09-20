package com.djmart.service.impl;

import com.djmart.dao.ProductDAO;
import com.djmart.dao.ReviewDAO;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.model.Product;
import com.djmart.service.ProductService;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service implementation for marketplace product catalog.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;

    public ProductServiceImpl(ProductDAO productDAO, ReviewDAO reviewDAO) {
        this.productDAO = productDAO;
        this.reviewDAO = reviewDAO;
    }

    @Override
    public ProductResponse createProduct(Long sellerId, ProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        validateProductInput(sellerId, request);

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setStockQty(request.getStockQty() != null ? request.getStockQty() : 0);
        product.setCategory(request.getCategory().trim());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);

        Product created = productDAO.create(product);
        LOGGER.info("Created product listing ID: {} by seller ID: {}", created.getId(), sellerId);

        return enrichProductResponse(created);
    }

    @Override
    public ProductResponse updateProduct(Long sellerId, Long productId, ProductRequest request, boolean isAdmin) {
        if (productId == null || request == null) {
            throw new IllegalArgumentException("Product ID and request cannot be null");
        }

        validateProductInput(sellerId, request);

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Seller can manage only their own products (unless Admin)
        if (!existing.getSellerId().equals(sellerId) && !isAdmin) {
            LOGGER.warn("Unauthorized attempt to update product ID {} by user ID {}", productId, sellerId);
            throw new AuthorizationException("You are not authorized to modify this product listing");
        }

        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        existing.setPrice(request.getPrice());
        existing.setStockQty(request.getStockQty());
        existing.setCategory(request.getCategory().trim());
        existing.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);

        productDAO.update(existing);
        LOGGER.info("Updated product ID: {} by user ID: {}", productId, sellerId);

        return enrichProductResponse(existing);
    }

    @Override
    public boolean deleteProduct(Long sellerId, Long productId, boolean isAdmin) {
        if (productId == null) {
            return false;
        }

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Ownership authorization check
        if (!existing.getSellerId().equals(sellerId) && !isAdmin) {
            LOGGER.warn("Unauthorized attempt to delete product ID {} by user ID {}", productId, sellerId);
            throw new AuthorizationException("You are not authorized to delete this product listing");
        }

        boolean deleted = productDAO.delete(productId);
        LOGGER.info("Deleted product listing ID: {} by user ID: {}", productId, sellerId);
        return deleted;
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        if (productId == null) {
            throw new ProductNotFoundException("Product ID cannot be null");
        }

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return enrichProductResponse(product);
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(String keyword, String category,
                                                        BigDecimal minPrice, BigDecimal maxPrice,
                                                        String sortBy, String sortOrder,
                                                        int page, int size) {
        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        String sanitizedKeyword = ValidationUtil.sanitizeSearchQuery(keyword);

        List<Product> products = productDAO.search(
                sanitizedKeyword.isEmpty() ? null : sanitizedKeyword,
                category != null && !category.trim().isEmpty() ? category.trim() : null,
                minPrice,
                maxPrice,
                sortBy,
                sortOrder,
                offset,
                safeSize
        );

        long total = productDAO.countSearch(
                sanitizedKeyword.isEmpty() ? null : sanitizedKeyword,
                category != null && !category.trim().isEmpty() ? category.trim() : null,
                minPrice,
                maxPrice
        );

        List<ProductResponse> dtos = products.stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }

    @Override
    public PageResponse<ProductResponse> getProductsBySeller(Long sellerId, int page, int size) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID cannot be null");
        }

        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<Product> products = productDAO.findBySeller(sellerId, offset, safeSize);
        long total = productDAO.countBySeller(sellerId);

        List<ProductResponse> dtos = products.stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }

    @Override
    public List<String> getCategories() {
        return productDAO.findDistinctCategories();
    }

    private void validateProductInput(Long sellerId, ProductRequest request) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(sellerId, "sellerId", errors);
        ValidationUtil.validateProductName(request.getName(), errors);
        ValidationUtil.validatePrice(request.getPrice(), errors);
        ValidationUtil.validateStockQty(request.getStockQty(), errors);
        ValidationUtil.validateRequired(request.getCategory(), "category", errors);
        ValidationUtil.validateDescription(request.getDescription(), errors);
        errors.throwIfHasErrors("Product validation failed");
    }

    private ProductResponse enrichProductResponse(Product product) {
        ProductResponse dto = ProductResponse.fromProduct(product);
        if (dto != null && reviewDAO != null) {
            double avgRating = reviewDAO.calculateAverageRating(product.getId());
            long count = reviewDAO.countByProduct(product.getId());
            dto.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
            dto.setReviewCount((int) count);
        }
        return dto;
    }
}
