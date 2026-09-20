package com.djmart.dao.jdbc;

import com.djmart.dao.BaseDAO;
import com.djmart.dao.ReviewDAO;
import com.djmart.dto.ReviewResponse;
import com.djmart.exception.DatabaseException;
import com.djmart.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ReviewDAO with joined user details and rating aggregation.
 */
public class ReviewDAOImpl extends BaseDAO implements ReviewDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDAOImpl.class);

    private static final String SQL_INSERT =
            "INSERT INTO reviews (product_id, user_id, rating, comment, created_at) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, product_id, user_id, rating, comment, created_at FROM reviews WHERE id = ?";

    private static final String SQL_FIND_BY_PRODUCT_JOIN_USER =
            "SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, r.created_at, u.name AS user_name " +
            "FROM reviews r " +
            "JOIN users u ON r.user_id = u.id " +
            "WHERE r.product_id = ? " +
            "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT_BY_PRODUCT =
            "SELECT COUNT(*) FROM reviews WHERE product_id = ?";

    private static final String SQL_CALCULATE_AVG_RATING =
            "SELECT COALESCE(AVG(CAST(rating AS DOUBLE)), 0.0) FROM reviews WHERE product_id = ?";

    private static final String SQL_EXISTS_BY_USER_AND_PRODUCT =
            "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ? LIMIT 1";

    private static final String SQL_DELETE =
            "DELETE FROM reviews WHERE id = ?";

    @Override
    public Review create(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating review failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setId(rs.getLong(1));
                }
            }
            return findById(review.getId()).orElse(review);
        } catch (SQLException e) {
            LOGGER.error("Failed to insert review for product {}: {}", review.getProductId(), e.getMessage(), e);
            throw new DatabaseException("Failed to persist review", e);
        }
    }

    @Override
    public Optional<Review> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Review review = new Review();
                    review.setId(rs.getLong("id"));
                    review.setProductId(rs.getLong("product_id"));
                    review.setUserId(rs.getLong("user_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(review);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.error("Failed to find review by ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve review", e);
        }
    }

    @Override
    public List<ReviewResponse> findByProduct(Long productId, int offset, int limit) {
        List<ReviewResponse> reviews = new ArrayList<>();
        if (productId == null) {
            return reviews;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PRODUCT_JOIN_USER)) {
            ps.setLong(1, productId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReviewResponse dto = new ReviewResponse();
                    dto.setId(rs.getLong("id"));
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setUserId(rs.getLong("user_id"));
                    dto.setUserName(rs.getString("user_name"));
                    dto.setRating(rs.getInt("rating"));
                    dto.setComment(rs.getString("comment"));
                    dto.setCreatedAt(rs.getTimestamp("created_at"));
                    reviews.add(dto);
                }
            }
            return reviews;
        } catch (SQLException e) {
            LOGGER.error("Failed to find reviews for product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to retrieve reviews", e);
        }
    }

    @Override
    public long countByProduct(Long productId) {
        if (productId == null) {
            return 0;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_PRODUCT)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to count reviews for product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to count reviews", e);
        }
    }

    @Override
    public double calculateAverageRating(Long productId) {
        if (productId == null) {
            return 0.0;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CALCULATE_AVG_RATING)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            return 0.0;
        } catch (SQLException e) {
            LOGGER.error("Failed to calculate average rating for product {}: {}", productId, e.getMessage(), e);
            throw new DatabaseException("Failed to calculate average rating", e);
        }
    }

    @Override
    public boolean existsByUserAndProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_USER_AND_PRODUCT)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to verify existing review for user {} and product {}: {}", userId, productId, e.getMessage(), e);
            throw new DatabaseException("Failed to verify existing review", e);
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
            LOGGER.error("Failed to delete review {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Failed to delete review", e);
        }
    }
}
