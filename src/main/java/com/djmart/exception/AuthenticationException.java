package com.djmart.exception;

/**
 * Thrown when credentials fail or when a route requires authentication and none is provided.
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(401, "UNAUTHENTICATED", message);
    }
}
