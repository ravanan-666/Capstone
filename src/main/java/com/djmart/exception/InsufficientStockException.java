package com.djmart.exception;

/**
 * Thrown when an order or cart operation cannot be fulfilled due to insufficient inventory.
 * Maps to HTTP 409 Conflict.
 */
public class InsufficientStockException extends AppException {

    private final Long productId;
    private final int requestedQty;
    private final int availableQty;

    public InsufficientStockException(Long productId, int requestedQty, int availableQty) {
        super(409, "INSUFFICIENT_STOCK",
                "Product ID " + productId + " has insufficient stock. Requested: " + requestedQty + ", Available: " + availableQty);
        this.productId = productId;
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
    }

    public InsufficientStockException(String message) {
        super(409, "INSUFFICIENT_STOCK", message);
        this.productId = null;
        this.requestedQty = 0;
        this.availableQty = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRequestedQty() {
        return requestedQty;
    }

    public int getAvailableQty() {
        return availableQty;
    }
}
