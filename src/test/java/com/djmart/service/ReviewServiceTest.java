package com.djmart.service;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dao.ReviewDAO;
import com.djmart.dto.OrderItemResponse;
import com.djmart.dto.ReviewRequest;
import com.djmart.dto.ReviewResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.ConflictException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Order;
import com.djmart.model.OrderStatus;
import com.djmart.model.Product;
import com.djmart.model.Review;
import com.djmart.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private OrderItemDAO orderItemDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewDAO, productDAO, orderDAO, orderItemDAO);
    }

    @Test
    @DisplayName("Add Review: Buyer with completed (DELIVERED) order successfully submits review")
    void testAddReviewSuccess() {
        Long buyerId = 4L;
        Long productId = 10L;

        ReviewRequest request = new ReviewRequest(productId, 5, "Outstanding craftsmanship and durable material!");

        when(productDAO.findById(productId)).thenReturn(Optional.of(new Product()));
        when(reviewDAO.existsByUserAndProduct(buyerId, productId)).thenReturn(false);

        // Buyer has a DELIVERED order
        Order deliveredOrder = new Order();
        deliveredOrder.setId(100L);
        deliveredOrder.setBuyerId(buyerId);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderDAO.findByBuyer(buyerId, 0, 100)).thenReturn(List.of(deliveredOrder));

        OrderItemResponse item = new OrderItemResponse(1L, productId, "Product", null, 1, new BigDecimal("50.00"));
        when(orderItemDAO.findByOrderId(100L)).thenReturn(List.of(item));

        Review saved = new Review(1L, productId, buyerId, 5, "Outstanding craftsmanship and durable material!", null);
        when(reviewDAO.create(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.addReview(buyerId, request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals(productId, response.getProductId());
        verify(reviewDAO).create(any(Review.class));
    }

    @Test
    @DisplayName("Add Review: Buyer without delivered order throws AuthorizationException")
    void testAddReviewWithoutDeliveredOrderThrows() {
        Long buyerId = 4L;
        Long productId = 10L;

        ReviewRequest request = new ReviewRequest(productId, 4, "Great product!");

        when(productDAO.findById(productId)).thenReturn(Optional.of(new Product()));
        when(reviewDAO.existsByUserAndProduct(buyerId, productId)).thenReturn(false);

        // Buyer has only a PENDING order, not DELIVERED
        Order pendingOrder = new Order();
        pendingOrder.setId(101L);
        pendingOrder.setBuyerId(buyerId);
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderDAO.findByBuyer(buyerId, 0, 100)).thenReturn(List.of(pendingOrder));

        assertThrows(AuthorizationException.class, () -> reviewService.addReview(buyerId, request));
        verify(reviewDAO, never()).create(any());
    }

    @Test
    @DisplayName("Add Review: Duplicate review submission throws ConflictException")
    void testAddReviewDuplicateThrows() {
        Long buyerId = 4L;
        Long productId = 10L;

        ReviewRequest request = new ReviewRequest(productId, 5, "Second review");

        when(productDAO.findById(productId)).thenReturn(Optional.of(new Product()));
        when(reviewDAO.existsByUserAndProduct(buyerId, productId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> reviewService.addReview(buyerId, request));
        verify(reviewDAO, never()).create(any());
    }

    @Test
    @DisplayName("Add Review: Invalid rating outside 1-5 throws ValidationException")
    void testAddReviewInvalidRatingThrows() {
        ReviewRequest zeroStars = new ReviewRequest(1L, 0, "Too low rating");
        assertThrows(ValidationException.class, () -> reviewService.addReview(4L, zeroStars));

        ReviewRequest sixStars = new ReviewRequest(1L, 6, "Too high rating");
        assertThrows(ValidationException.class, () -> reviewService.addReview(4L, sixStars));
    }
}
