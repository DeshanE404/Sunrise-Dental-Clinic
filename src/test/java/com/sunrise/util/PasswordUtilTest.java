package com.sunrise.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashPasswordReturnsBcryptHashThatVerifies() {
        String hash = PasswordUtil.hashPassword("Clinic@2026");
        assertTrue(hash.startsWith("$2a$"), "BCrypt hashes start with $2a$");
        assertTrue(PasswordUtil.checkPassword("Clinic@2026", hash));
    }

    @Test
    void checkPasswordRejectsWrongPassword() {
        String hash = PasswordUtil.hashPassword("Clinic@2026");
        assertFalse(PasswordUtil.checkPassword("WrongPass", hash));
    }

    @Test
    void checkPasswordHandlesNullAndMalformedHashesSafely() {
        assertFalse(PasswordUtil.checkPassword("anything", null));
        assertFalse(PasswordUtil.checkPassword("anything", "not-a-bcrypt-hash"));
    }
}
