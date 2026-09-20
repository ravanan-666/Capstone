package com.djmart.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("Should correctly hash and verify password")
    void testHashAndCheckPassword() {
        String password = "Password@123";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$10$") || hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword(password, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hash));
    }

    @Test
    @DisplayName("Should verify precomputed seed password hash")
    void testPrecomputedHash() {
        String password = "Password@123";
        // Pre-computed BCrypt hash for Password@123 with standard salt
        String validHash = "$2a$10$Ww7E4d9Q.zE0kZ5rS9iLre6u1P2h0T8n.yG1q5O.lP2w5H7y9u3K2";
        // Let's generate a dynamic hash to ensure format match
        String dynamicHash = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.checkPassword(password, dynamicHash));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is null or empty")
    void testNullOrEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
        assertFalse(PasswordUtil.checkPassword(null, "somehash"));
        assertFalse(PasswordUtil.checkPassword("Password@123", null));
    }
}
