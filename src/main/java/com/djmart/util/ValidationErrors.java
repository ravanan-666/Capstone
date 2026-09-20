package com.djmart.util;

import com.djmart.exception.ValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container holding field-level validation errors.
 */
public class ValidationErrors {

    private final Map<String, String> errors = new LinkedHashMap<>();

    public void addError(String field, String message) {
        if (field != null && message != null) {
            // Keep first error message if multiple are added for same field
            errors.putIfAbsent(field, message);
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    /**
     * Throws a ValidationException if any field errors have been collected.
     *
     * @param generalMessage summary error message
     * @throws ValidationException if hasErrors() is true
     */
    public void throwIfHasErrors(String generalMessage) {
        if (hasErrors()) {
            throw new ValidationException(generalMessage, errors);
        }
    }
}
