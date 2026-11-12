package com.flashcards.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

// Checks login tokens issued by the user service. Any service that receives
// tokens uses this; only the user service creates them.
//
// Tokens are verified with the same shared secret (HS256) the user service signs
// with, so a service can check a token locally without calling the user service.
public class JwtVerifier {

    private final SecretKey key;

    public JwtVerifier(String secret) {
        // The same key-building the user service signs with (see JwtKeys).
        this.key = JwtKeys.fromSecret(secret);
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
