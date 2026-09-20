package com.djmart.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO representing an item in the buyer's shopping cart.
 */
public class CartItemResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public CartItemResponse() {
    }

    public CartItemResponse(Long id, Long productId, String productName, String productImageUrl,
                            BigDecimal unitPrice, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        updateSubtotal();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        updateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    private void updateSubtotal() {
        if (this.unitPrice != null && this.quantity != null) {
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    @Override
    public String toString() {
        return "CartItemResponse{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
