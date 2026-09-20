package com.djmart.dao;

import com.djmart.dto.ReviewResponse;
import com.djmart.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Review entities.
 */
public interface ReviewDAO {

    /**
     * Persists a new review and rating.
     */
    Review create(Review review);

    /**
     * Finds a review by its primary key ID.
     */
    Optional<Review> findById(Long id);

    /**
     * Finds reviews for a product with buyer name joined to prevent N+1 queries.
     */
    List<ReviewResponse> findByProduct(Long productId, int offset, int limit);

    /**
     * Counts total reviews for a specific product.
     */
    long countByProduct(Long productId);

    /**
     * Computes average star rating for a product (defaults to 0.0 if no reviews).
     */
    double calculateAverageRating(Long productId);

    /**
     * Checks if a user has already reviewed a given product.
     */
    boolean existsByUserAndProduct(Long userId, Long productId);

    /**
     * Deletes a review (for admin moderation).
     */
    boolean delete(Long id);
}
