package com.djmart.dto;

import java.io.Serializable;

/**
 * DTO for adding or updating an item in the buyer's cart.
 */
public class CartItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;

    public CartItemRequest() {
    }

    public CartItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItemRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
