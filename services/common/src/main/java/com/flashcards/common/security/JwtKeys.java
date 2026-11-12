package com.flashcards.common.security;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// Turns the shared JWT secret into a signing key. Both sides of the token contract
// use this one method: the user service to sign tokens, and JwtVerifier to check
// them. Keeping it in one place means the two can never build the key differently,
// which would make every token silently fail verification.
public final class JwtKeys {

    // HS256 requires a key of at least 256 bits.
    private static final int MIN_SECRET_BYTES = 32;

    private JwtKeys() {
    }

    public static SecretKey fromSecret(String secret) {
        if (secret == null) {
            throw new IllegalStateException("JWT secret must be set");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
