package com.flashcards.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// Checks login tokens issued by the user service. Any service that receives
// tokens uses this; only the user service creates them.
//
// Tokens are verified with the same shared secret (HS256) the user service signs
// with, so a service can check a token locally without calling the user service.
public class JwtVerifier {

    private final SecretKey key;

    public JwtVerifier(String secret) {
        if (secret == null) {
            throw new IllegalStateException("JWT secret must be set");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Same minimum as the user service: HS256 needs a key of at least 256 bits.
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Returns who the token belongs to, or throws InvalidTokenException if it is
    // expired, tampered with, signed with a different secret, or not a token at all.
    public AuthenticatedUser verify(String token) {
        try {
            // verifyWith checks the signature and the expiry date in one step.
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new AuthenticatedUser(
                    Long.parseLong(claims.getSubject()),
                    claims.get("username", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            // IllegalArgumentException also covers a null/empty token or a
            // subject that is not a number.
            throw new InvalidTokenException();
        }
    }
}
