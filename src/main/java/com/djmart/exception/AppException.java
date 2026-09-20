package com.djmart.exception;

/**
 * Base abstract exception for application and API errors.
 * Encapsulates an HTTP status code, a domain error code, and error details.
 */
public abstract class AppException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public AppException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public AppException(int statusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
