package com.djmart.dto;

import com.djmart.model.Review;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Response DTO representing product reviews and ratings.
 */
public class ReviewResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;

    public ReviewResponse() {
    }

    public static ReviewResponse fromReview(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse dto = new ReviewResponse();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ReviewResponse{" +
                "id=" + id +
                ", productId=" + productId +
                ", rating=" + rating +
                ", userName='" + userName + '\'' +
                '}';
    }
}
