package com.djmart.exception;

/**
 * Thrown when an authenticated user attempts to access a resource beyond their role permissions.
 * Maps to HTTP 403 Forbidden.
 */
public class AuthorizationException extends AppException {

    public AuthorizationException(String message) {
        super(403, "FORBIDDEN", message);
    }
}
