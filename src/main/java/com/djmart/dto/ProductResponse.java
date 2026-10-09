package com.djmart.dto;

import com.djmart.model.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Response DTO representing product catalog and detail views.
 */
public class ProductResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private String brand;
    private String sku;
    private BigDecimal originalPrice;
    private String currency = "INR";
    private Timestamp priceVerifiedAt;
    private Integer discountPercent;
    private Double averageRating;
    private Integer reviewCount;
    private Timestamp createdAt;

    public ProductResponse() {
    }

    public ProductResponse(Long id, Long sellerId, String sellerName, String name, String description,
                           BigDecimal price, Integer stockQty, String category, String imageUrl,
                           Double averageRating, Integer reviewCount, Timestamp createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.createdAt = createdAt;
    }

    public static ProductResponse fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setSellerId(product.getSellerId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQty(product.getStockQty());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setBrand(product.getBrand());
        dto.setSku(product.getSku());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCurrency(product.getCurrency() != null ? product.getCurrency() : "INR");
        dto.setPriceVerifiedAt(product.getPriceVerifiedAt());
        if (product.getOriginalPrice() != null && product.getPrice() != null &&
                product.getOriginalPrice().compareTo(product.getPrice()) > 0) {
            BigDecimal diff = product.getOriginalPrice().subtract(product.getPrice());
            int pct = diff.multiply(BigDecimal.valueOf(100))
                    .divide(product.getOriginalPrice(), 0, java.math.RoundingMode.HALF_UP).intValue();
            dto.setDiscountPercent(pct);
        }
        return dto;
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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
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

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
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

    public Timestamp getPriceVerifiedAt() {
        return priceVerifiedAt;
    }

    public void setPriceVerifiedAt(Timestamp priceVerifiedAt) {
        this.priceVerifiedAt = priceVerifiedAt;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", category='" + category + '\'' +
                '}';
    }
}
