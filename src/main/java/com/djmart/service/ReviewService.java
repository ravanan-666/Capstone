package com.djmart.service;

import com.djmart.dto.PageResponse;
import com.djmart.dto.ReviewRequest;
import com.djmart.dto.ReviewResponse;

/**
 * Service interface managing buyer reviews and ratings on completed orders.
 */
public interface ReviewService {

    /**
     * Submits a new review and rating on a product.
     * Enforces eligibility: Buyer must have purchased and received (DELIVERED) the product,
     * and must not have previously reviewed it.
     */
    ReviewResponse addReview(Long buyerId, ReviewRequest request);

    /**
     * Checks whether a buyer is eligible to review a product.
     */
    boolean isEligibleToReview(Long buyerId, Long productId);

    /**
     * Retrieves paginated reviews for a product with reviewer names.
     */
    PageResponse<ReviewResponse> getReviewsByProduct(Long productId, int page, int size);

    /**
     * Deletes a review. Permitted for the author or an Admin.
     */
    boolean deleteReview(Long reviewId, Long userId, boolean isAdmin);
}
