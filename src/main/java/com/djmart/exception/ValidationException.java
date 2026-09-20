package com.djmart.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when user or client input fails domain validation rules.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends AppException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(400, "VALIDATION_ERROR", message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(400, "VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
