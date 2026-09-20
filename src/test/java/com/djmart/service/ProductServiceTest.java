package com.djmart.service;

import com.djmart.dao.ProductDAO;
import com.djmart.dao.ReviewDAO;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ProductRequest;
import com.djmart.dto.ProductResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Product;
import com.djmart.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private ReviewDAO reviewDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productDAO, reviewDAO);
    }

    @Test
    @DisplayName("Create Product: Valid request persists product")
    void testCreateProductSuccess() {
        ProductRequest request = new ProductRequest(
                "Noise-Canceling Headphones",
                "High quality audio with ANC.",
                new BigDecimal("199.99"),
                50,
                "Electronics",
                "https://example.com/headphones.jpg"
        );

        Product saved = new Product(
                1L, 2L, request.getName(), request.getDescription(),
                request.getPrice(), request.getStockQty(), request.getCategory(),
                request.getImageUrl(), Timestamp.from(Instant.now())
        );

        when(productDAO.create(any(Product.class))).thenReturn(saved);
        when(reviewDAO.calculateAverageRating(1L)).thenReturn(4.8);
        when(reviewDAO.countByProduct(1L)).thenReturn(12L);

        ProductResponse response = productService.createProduct(2L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Noise-Canceling Headphones", response.getName());
        assertEquals(4.8, response.getAverageRating());
        assertEquals(12, response.getReviewCount());
    }

    @Test
    @DisplayName("Create Product: Negative price or invalid stock throws ValidationException")
    void testCreateProductValidationFailure() {
        ProductRequest invalidPrice = new ProductRequest(
                "Headphones", "Desc", new BigDecimal("-10.00"), 5, "Electronics", null
        );
        assertThrows(ValidationException.class, () -> productService.createProduct(2L, invalidPrice));

        ProductRequest invalidStock = new ProductRequest(
                "Headphones", "Desc", new BigDecimal("10.00"), -1, "Electronics", null
        );
        assertThrows(ValidationException.class, () -> productService.createProduct(2L, invalidStock));
    }

    @Test
    @DisplayName("Update Product: Seller can only update their own product")
    void testUpdateProductOwnershipEnforcement() {
        Long ownerSellerId = 2L;
        Long otherSellerId = 3L;
        Long productId = 10L;

        Product existing = new Product(productId, ownerSellerId, "Original Name", "Desc",
                new BigDecimal("50.00"), 10, "Electronics", null, Timestamp.from(Instant.now()));

        when(productDAO.findById(productId)).thenReturn(Optional.of(existing));

        ProductRequest updateReq = new ProductRequest(
                "Updated Name", "Updated Desc", new BigDecimal("55.00"), 12, "Electronics", null
        );

        // Another seller attempting update without admin privileges throws AuthorizationException
        assertThrows(AuthorizationException.class,
                () -> productService.updateProduct(otherSellerId, productId, updateReq, false));

        // Owner seller succeeds
        ProductResponse response = productService.updateProduct(ownerSellerId, productId, updateReq, false);
        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        verify(productDAO).update(existing);

        // Admin can update regardless of seller ownership
        ProductResponse adminUpdated = productService.updateProduct(999L, productId, updateReq, true);
        assertNotNull(adminUpdated);
    }

    @Test
    @DisplayName("Delete Product: Unauthorized seller cannot delete other's listing")
    void testDeleteProductOwnership() {
        Product existing = new Product(5L, 2L, "Product", "Desc", new BigDecimal("20.00"), 5, "Fashion", null, null);
        when(productDAO.findById(5L)).thenReturn(Optional.of(existing));

        // Stranger seller
        assertThrows(AuthorizationException.class, () -> productService.deleteProduct(99L, 5L, false));

        // Owner seller
        when(productDAO.delete(5L)).thenReturn(true);
        assertTrue(productService.deleteProduct(2L, 5L, false));
    }

    @Test
    @DisplayName("Search Products: Returns paginated response with counts")
    void testSearchProducts() {
        Product p1 = new Product(1L, 2L, "Keyboard A", "Desc", new BigDecimal("50.00"), 10, "Electronics", null, null);
        Product p2 = new Product(2L, 2L, "Keyboard B", "Desc", new BigDecimal("80.00"), 15, "Electronics", null, null);

        when(productDAO.search(eq("Keyboard"), eq("Electronics"), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(List.of(p1, p2));
        when(productDAO.countSearch(eq("Keyboard"), eq("Electronics"), any(), any())).thenReturn(2L);

        PageResponse<ProductResponse> page = productService.searchProducts(
                "Keyboard", "Electronics", null, null, "price", "ASC", 1, 10
        );

        assertEquals(1, page.getPageNumber());
        assertEquals(2, page.getContent().size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
    }
}
