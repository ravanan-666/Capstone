package com.djmart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and validating passwords using jBCrypt.
 * Strictly enforces modern salted hashing (no plaintext, no MD5/SHA1).
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 10;

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hash a plain-text password using BCrypt with salt rounds = 10.
     *
     * @param plainPassword the plain text password
     * @return the hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verify a plain text password against a stored BCrypt hash.
     *
     * @param plainPassword the plain text password candidate
     * @param hashedPassword the stored BCrypt hash
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }
}
