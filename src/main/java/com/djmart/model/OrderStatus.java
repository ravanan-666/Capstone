package com.djmart.model;

/**
 * Order status states conforming to the marketplace workflow.
 * Workflow: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED (or CANCELLED).
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        return OrderStatus.valueOf(value.trim().toUpperCase());
    }
}
