package com.flashcards.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtKeysTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long!";

    @Test
    @DisplayName("The same secret always produces the same key (signer and verifier agree)")
    void sameSecretSameKey() {
        assertArrayEquals(JwtKeys.fromSecret(SECRET).getEncoded(), JwtKeys.fromSecret(SECRET).getEncoded());
    }

    @Test
    @DisplayName("A missing secret is refused")
    void rejectsNullSecret() {
        assertThrows(IllegalStateException.class, () -> JwtKeys.fromSecret(null));
    }

    @Test
    @DisplayName("A secret shorter than 32 bytes is refused")
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> JwtKeys.fromSecret("too-short"));
    }
}
