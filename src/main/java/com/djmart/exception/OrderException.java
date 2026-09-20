package com.djmart.exception;

/**
 * Thrown when an order operation fails business validation or status transition rules.
 * Maps to HTTP 400 Bad Request by default.
 */
public class OrderException extends AppException {

    public OrderException(String message) {
        super(400, "ORDER_ERROR", message);
    }

    public OrderException(int statusCode, String errorCode, String message) {
        super(statusCode, errorCode, message);
    }
}
