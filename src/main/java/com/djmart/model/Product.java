package com.djmart.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Domain entity representing a product listing by a seller.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private Timestamp createdAt;

    private String brand;
    private String sku;
    private BigDecimal originalPrice;
    private String currency = "INR";
    private Boolean isActive = true;
    private Timestamp priceVerifiedAt;
    private Timestamp updatedAt;

    public Product() {
    }

    public Product(Long id, Long sellerId, String name, String description,
                   BigDecimal price, Integer stockQty, String category,
                   String imageUrl, Timestamp createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getPriceVerifiedAt() {
        return priceVerifiedAt;
    }

    public void setPriceVerifiedAt(Timestamp priceVerifiedAt) {
        this.priceVerifiedAt = priceVerifiedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sellerId=" + sellerId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}
