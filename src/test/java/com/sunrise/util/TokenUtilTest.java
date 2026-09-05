package com.sunrise.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenUtilTest {

    @Test
    void generateTokenProduces64HexCharacters() {
        String token = TokenUtil.generateToken();
        assertEquals(64, token.length());
        assertTrue(token.matches("[0-9a-f]{64}"));
    }

    @Test
    void generateTokenIsUniquePerCall() {
        assertNotEquals(TokenUtil.generateToken(), TokenUtil.generateToken());
    }

    @Test
    void sha256HexIsDeterministicAndStable() {
        String digestOne = TokenUtil.sha256Hex("sample-token");
        String digestTwo = TokenUtil.sha256Hex("sample-token");
        assertEquals(digestOne, digestTwo);
        assertEquals(64, digestOne.length());
        assertNotEquals(digestOne, TokenUtil.sha256Hex("different-token"));
    }
}
