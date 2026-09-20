package com.djmart.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing a buyer's review and rating on a product.
 */
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;

    // Optional transient fields for presentation
    private String userName;

    public Review() {
    }

    public Review(Long id, Long productId, Long userId, Integer rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", productId=" + productId +
                ", userId=" + userId +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
