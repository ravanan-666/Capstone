package com.djmart.exception;

/**
 * Thrown when a requested resource (user, order, category, etc.) does not exist.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(404, "RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String errorCode, String message) {
        super(404, errorCode, message);
    }
}
