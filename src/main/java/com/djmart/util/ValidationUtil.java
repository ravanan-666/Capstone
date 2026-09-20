package com.djmart.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reusable input validation utility enforcing all business constraints
 * before Service and DAO calls are executed.
 */
public final class ValidationUtil {

    // RFC 5322 compliant simplified email regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Password rule: min 8 chars, at least 1 upper, 1 lower, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_-])[A-Za-z\\d@$!%*?&#^()_-]{8,}$"
    );

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static void validateRequired(String value, String fieldName, ValidationErrors errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.addError(fieldName, fieldName + " is required");
        }
    }

    public static void validateEmail(String email, ValidationErrors errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.addError("email", "Email is required");
            return;
        }
        String trimmed = email.trim();
        if (trimmed.length() > 255) {
            errors.addError("email", "Email must not exceed 255 characters");
            return;
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            errors.addError("email", "Invalid email address format");
        }
    }

    public static void validatePassword(String password, ValidationErrors errors) {
        if (password == null || password.isEmpty()) {
            errors.addError("password", "Password is required");
            return;
        }
        if (password.length() < 8) {
            errors.addError("password", "Password must be at least 8 characters long");
            return;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            errors.addError("password",
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }

    public static void validatePasswordMatch(String password, String confirmPassword, ValidationErrors errors) {
        if (password != null && !password.equals(confirmPassword)) {
            errors.addError("confirmPassword", "Passwords do not match");
        }
    }

    public static void validateProductName(String name, ValidationErrors errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.addError("name", "Product name is required");
            return;
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 255) {
            errors.addError("name", "Product name must be between 2 and 255 characters");
        }
    }

    public static void validateDescription(String description, ValidationErrors errors) {
        if (description != null && description.length() > 4000) {
            errors.addError("description", "Description must not exceed 4000 characters");
        }
    }

    public static void validatePrice(BigDecimal price, ValidationErrors errors) {
        if (price == null) {
            errors.addError("price", "Price is required");
            return;
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            errors.addError("price", "Price cannot be negative");
            return;
        }
        if (price.scale() > 2) {
            errors.addError("price", "Price cannot have more than 2 decimal places");
            return;
        }
        if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
            errors.addError("price", "Price exceeds maximum allowable limit");
        }
    }

    public static void validateStockQty(Integer stockQty, ValidationErrors errors) {
        if (stockQty == null) {
            errors.addError("stockQty", "Stock quantity is required");
            return;
        }
        if (stockQty < 0) {
            errors.addError("stockQty", "Stock quantity cannot be negative");
            return;
        }
        if (stockQty > 1_000_000) {
            errors.addError("stockQty", "Stock quantity exceeds maximum allowable limit");
        }
    }

    public static void validateCartQuantity(Integer quantity, ValidationErrors errors) {
        if (quantity == null) {
            errors.addError("quantity", "Cart quantity is required");
            return;
        }
        if (quantity < 1) {
            errors.addError("quantity", "Cart quantity must be at least 1");
            return;
        }
        if (quantity > 99) {
            errors.addError("quantity", "Cart quantity cannot exceed 99 items per product");
        }
    }

    public static void validateRating(Integer rating, ValidationErrors errors) {
        if (rating == null) {
            errors.addError("rating", "Rating is required");
            return;
        }
        if (rating < 1 || rating > 5) {
            errors.addError("rating", "Rating must be between 1 and 5 stars");
        }
    }

    public static void validateComment(String comment, ValidationErrors errors) {
        if (comment != null && comment.length() > 1000) {
            errors.addError("comment", "Review comment must not exceed 1000 characters");
        }
    }

    public static void validateId(Long id, String fieldName, ValidationErrors errors) {
        if (id == null || id <= 0) {
            errors.addError(fieldName, fieldName + " must be a positive integer");
        }
    }

    public static int[] validatePagination(Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 10 : Math.min(size, 100);
        return new int[]{safePage, safeSize};
    }

    public static String sanitizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        String sanitized = query.trim();
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        // Remove control characters
        sanitized = sanitized.replaceAll("[\\p{Cntrl}]", "");
        return sanitized;
    }
}
