package com.djmart.exception;

/**
 * Thrown when a business conflict occurs (such as duplicate unique resources or illegal state transitions).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(409, "CONFLICT", message);
    }

    public ConflictException(String errorCode, String message) {
        super(409, errorCode, message);
    }
}
