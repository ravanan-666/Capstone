package com.djmart.service.impl;

import com.djmart.dao.OrderDAO;
import com.djmart.dao.OrderItemDAO;
import com.djmart.dao.ProductDAO;
import com.djmart.dao.ReviewDAO;
import com.djmart.dto.OrderItemResponse;
import com.djmart.dto.PageResponse;
import com.djmart.dto.ReviewRequest;
import com.djmart.dto.ReviewResponse;
import com.djmart.exception.AuthorizationException;
import com.djmart.exception.ConflictException;
import com.djmart.exception.ProductNotFoundException;
import com.djmart.exception.ResourceNotFoundException;
import com.djmart.model.Order;
import com.djmart.model.OrderStatus;
import com.djmart.model.Review;
import com.djmart.service.ReviewService;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Business service implementation for product reviews and ratings on completed orders.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;

    public ReviewServiceImpl(ReviewDAO reviewDAO, ProductDAO productDAO,
                             OrderDAO orderDAO, OrderItemDAO orderItemDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
    }

    @Override
    public ReviewResponse addReview(Long buyerId, ReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Review request cannot be null");
        }

        // 1. Validate inputs
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(buyerId, "buyerId", errors);
        ValidationUtil.validateId(request.getProductId(), "productId", errors);
        ValidationUtil.validateRating(request.getRating(), errors);
        ValidationUtil.validateComment(request.getComment(), errors);
        errors.throwIfHasErrors("Review validation failed");

        // 2. Product must exist
        productDAO.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // 3. Prevent duplicate review
        if (reviewDAO.existsByUserAndProduct(buyerId, request.getProductId())) {
            LOGGER.warn("Duplicate review rejected: User {} already reviewed product {}", buyerId, request.getProductId());
            throw new ConflictException("You have already reviewed this product");
        }

        // 4. Verify completed order eligibility (F8: Reviews on completed orders only)
        if (!isEligibleToReview(buyerId, request.getProductId())) {
            LOGGER.warn("Review submission rejected: User {} has no delivered order for product {}",
                    buyerId, request.getProductId());
            throw new AuthorizationException("You can only review products from completed (DELIVERED) orders");
        }

        // 5. Persist review
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(buyerId);
        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);

        Review created = reviewDAO.create(review);
        LOGGER.info("Successfully added review ID {} for product ID {} by user ID {}",
                created.getId(), request.getProductId(), buyerId);

        return ReviewResponse.fromReview(created);
    }

    @Override
    public boolean isEligibleToReview(Long buyerId, Long productId) {
        if (buyerId == null || productId == null) {
            return false;
        }

        if (reviewDAO.existsByUserAndProduct(buyerId, productId)) {
            return false;
        }

        // Search buyer's past orders for delivered status containing this product
        List<Order> buyerOrders = orderDAO.findByBuyer(buyerId, 0, 100);
        for (Order order : buyerOrders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                List<OrderItemResponse> items = orderItemDAO.findByOrderId(order.getId());
                for (OrderItemResponse item : items) {
                    if (item.getProductId().equals(productId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public PageResponse<ReviewResponse> getReviewsByProduct(Long productId, int page, int size) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<ReviewResponse> reviews = reviewDAO.findByProduct(productId, offset, safeSize);
        long total = reviewDAO.countByProduct(productId);

        return new PageResponse<>(reviews, safePage, safeSize, total);
    }

    @Override
    public boolean deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        if (reviewId == null) {
            return false;
        }

        Review review = reviewDAO.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUserId().equals(userId) && !isAdmin) {
            LOGGER.warn("Unauthorized review deletion attempt: Review {} by user {}", reviewId, userId);
            throw new AuthorizationException("You are not authorized to delete this review");
        }

        return reviewDAO.delete(reviewId);
    }
}
