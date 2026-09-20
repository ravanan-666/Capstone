package com.djmart.exception;

/**
 * Thrown when a specified product is not found in the catalog.
 * Maps to HTTP 404 Not Found with error code PRODUCT_NOT_FOUND.
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", "Product with ID " + productId + " was not found");
    }

    public ProductNotFoundException(String message) {
        super("PRODUCT_NOT_FOUND", message);
    }
}
