package com.djmart.util;

import com.djmart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    @DisplayName("Validate email address format")
    void testEmailValidation() {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateEmail("valid.user@example.com", errors);
        assertFalse(errors.hasErrors());

        ValidationErrors invalidErrors = new ValidationErrors();
        ValidationUtil.validateEmail("invalid-email", invalidErrors);
        ValidationUtil.validateEmail("", invalidErrors);
        ValidationUtil.validateEmail(null, invalidErrors);
        assertTrue(invalidErrors.hasErrors());
        assertTrue(invalidErrors.getErrors().containsKey("email"));
    }

    @Test
    @DisplayName("Validate password complexity rules")
    void testPasswordValidation() {
        // Valid password: >= 8 chars, 1 upper, 1 lower, 1 digit, 1 special char
        ValidationErrors valid = new ValidationErrors();
        ValidationUtil.validatePassword("Password@123", valid);
        assertFalse(valid.hasErrors());

        // Invalid: missing special char
        ValidationErrors noSpecial = new ValidationErrors();
        ValidationUtil.validatePassword("Password123", noSpecial);
        assertTrue(noSpecial.hasErrors());

        // Invalid: missing uppercase
        ValidationErrors noUpper = new ValidationErrors();
        ValidationUtil.validatePassword("password@123", noUpper);
        assertTrue(noUpper.hasErrors());

        // Invalid: too short
        ValidationErrors tooShort = new ValidationErrors();
        ValidationUtil.validatePassword("Pass@1", tooShort);
        assertTrue(tooShort.hasErrors());
    }

    @Test
    @DisplayName("Validate password confirmation matching")
    void testPasswordMatch() {
        ValidationErrors match = new ValidationErrors();
        ValidationUtil.validatePasswordMatch("Secret#123", "Secret#123", match);
        assertFalse(match.hasErrors());

        ValidationErrors mismatch = new ValidationErrors();
        ValidationUtil.validatePasswordMatch("Secret#123", "Different#123", mismatch);
        assertTrue(mismatch.hasErrors());
        assertTrue(mismatch.getErrors().containsKey("confirmPassword"));
    }

    @Test
    @DisplayName("Validate product price and decimal precision")
    void testPriceValidation() {
        ValidationErrors valid = new ValidationErrors();
        ValidationUtil.validatePrice(new BigDecimal("19.99"), valid);
        ValidationUtil.validatePrice(new BigDecimal("0.00"), valid);
        assertFalse(valid.hasErrors());

        ValidationErrors negative = new ValidationErrors();
        ValidationUtil.validatePrice(new BigDecimal("-5.00"), negative);
        assertTrue(negative.hasErrors());

        ValidationErrors excessScale = new ValidationErrors();
        ValidationUtil.validatePrice(new BigDecimal("19.999"), excessScale);
        assertTrue(excessScale.hasErrors());
    }

    @Test
    @DisplayName("Validate product stock quantity")
    void testStockQuantityValidation() {
        ValidationErrors valid = new ValidationErrors();
        ValidationUtil.validateStockQty(100, valid);
        ValidationUtil.validateStockQty(0, valid);
        assertFalse(valid.hasErrors());

        ValidationErrors negative = new ValidationErrors();
        ValidationUtil.validateStockQty(-1, negative);
        assertTrue(negative.hasErrors());

        ValidationErrors excess = new ValidationErrors();
        ValidationUtil.validateStockQty(1_500_000, excess);
        assertTrue(excess.hasErrors());
    }

    @Test
    @DisplayName("Validate cart quantity limits (1 to 99)")
    void testCartQuantityValidation() {
        ValidationErrors valid = new ValidationErrors();
        ValidationUtil.validateCartQuantity(1, valid);
        ValidationUtil.validateCartQuantity(99, valid);
        assertFalse(valid.hasErrors());

        ValidationErrors zero = new ValidationErrors();
        ValidationUtil.validateCartQuantity(0, zero);
        assertTrue(zero.hasErrors());

        ValidationErrors overLimit = new ValidationErrors();
        ValidationUtil.validateCartQuantity(100, overLimit);
        assertTrue(overLimit.hasErrors());
    }

    @Test
    @DisplayName("Validate review rating constraints (1 to 5)")
    void testReviewRatingValidation() {
        ValidationErrors valid = new ValidationErrors();
        for (int r = 1; r <= 5; r++) {
            ValidationUtil.validateRating(r, valid);
        }
        assertFalse(valid.hasErrors());

        ValidationErrors invalidLow = new ValidationErrors();
        ValidationUtil.validateRating(0, invalidLow);
        assertTrue(invalidLow.hasErrors());

        ValidationErrors invalidHigh = new ValidationErrors();
        ValidationUtil.validateRating(6, invalidHigh);
        assertTrue(invalidHigh.hasErrors());
    }

    @Test
    @DisplayName("Validate pagination parameters bounding")
    void testPaginationValidation() {
        int[] safe1 = ValidationUtil.validatePagination(null, null);
        assertEquals(1, safe1[0]);
        assertEquals(10, safe1[1]);

        int[] safe2 = ValidationUtil.validatePagination(-5, 500);
        assertEquals(1, safe2[0]);
        assertEquals(100, safe2[1]); // Capped at 100

        int[] safe3 = ValidationUtil.validatePagination(3, 25);
        assertEquals(3, safe3[0]);
        assertEquals(25, safe3[1]);
    }

    @Test
    @DisplayName("Validate search query sanitization")
    void testSearchQuerySanitization() {
        assertEquals("", ValidationUtil.sanitizeSearchQuery(null));
        assertEquals("laptop headphones", ValidationUtil.sanitizeSearchQuery("  laptop headphones  "));
        // Test length capping
        String longQuery = "a".repeat(150);
        assertEquals(100, ValidationUtil.sanitizeSearchQuery(longQuery).length());
    }

    @Test
    @DisplayName("ValidationErrors throws ValidationException with mapped field errors")
    void testThrowIfHasErrors() {
        ValidationErrors errors = new ValidationErrors();
        errors.addError("email", "Email is required");
        errors.addError("password", "Password is required");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> errors.throwIfHasErrors("Registration validation failed"));

        assertEquals(400, ex.getStatusCode());
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertEquals(2, ex.getFieldErrors().size());
        assertEquals("Email is required", ex.getFieldErrors().get("email"));
    }
}
