package com.sunrise.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Helpers for the DB-backed "remember me" auto-login feature.
 *
 * A random token is placed in an HttpOnly cookie and only a SHA-256 hash of the
 * token is stored in the database, so a leaked database never exposes usable
 * login tokens.
 */
public final class TokenUtil {
    /** How long a remember-me token stays valid (30 days). */
    public static final long REMEMBER_DURATION_MS = 30L * 24 * 60 * 60 * 1000;

    private static final int TOKEN_BYTES = 32;

    private TokenUtil() {
        // Utility class.
    }

    /** Generates a new cryptographically random token (64 hex characters). */
    public static String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    /** Returns the lowercase SHA-256 hex digest of the supplied value. */
    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}