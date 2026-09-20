package com.djmart.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility providing cryptographic token generation, XSS escaping, and security constants.
 */
public final class SecurityUtil {

    public static final String SESSION_USER = "user";
    public static final String SESSION_CSRF_TOKEN = "csrfToken";
    public static final String CSRF_HEADER = "X-CSRF-Token";
    public static final String CSRF_PARAM = "_csrf";
    public static final String MDC_REQUEST_ID = "requestId";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Generates a cryptographically strong pseudo-random token (e.g. for CSRF protection).
     *
     * @return URL-safe base64 encoded token
     */
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Escapes characters with HTML entities to protect against Cross-Site Scripting (XSS).
     *
     * @param input raw string
     * @return HTML-escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(Math.max(16, input.length()));
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '&' -> out.append("&amp;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#x27;");
                case '/' -> out.append("&#x2F;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Sanitizes strings for logging to prevent CRLF log injection vulnerabilities.
     *
     * @param input raw log input
     * @return safe single-line log string
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "";
        }
        return input.replace('\r', '_').replace('\n', '_');
    }
}
