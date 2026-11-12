package com.flashcards.user.service;

import com.flashcards.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

// Unit test — creates real tokens and verifies them independently with the jjwt parser.
class JwtServiceTest {

    private static final String TEST_SECRET = "test-only-secret-at-least-32-bytes-long!";

    // Verifies a token's signature with the given secret and returns its contents.
    // Throws if the token was not signed with that secret.
    static Claims parse(String token, String secret) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private User userWithId(long id, String username) {
        User user = new User(username, username.toLowerCase(), "irrelevant-hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("Token carries the user id, username, and a 24-hour expiry")
    void tokenContainsExpectedClaims() {
        JwtService jwtService = new JwtService(TEST_SECRET, 24);

        Claims claims = parse(jwtService.createToken(userWithId(7L, "SeanPeake")), TEST_SECRET);

        assertEquals("7", claims.getSubject());
        assertEquals("SeanPeake", claims.get("username", String.class));
        Duration lifetime = Duration.between(claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
        assertEquals(Duration.ofHours(24), lifetime);
    }

    @Test
    @DisplayName("Token signed with one secret fails verification with another")
    void tokenRejectedWithDifferentSecret() {
        JwtService jwtService = new JwtService(TEST_SECRET, 24);
        String token = jwtService.createToken(userWithId(7L, "SeanPeake"));

        assertThrows(SignatureException.class,
                () -> parse(token, "a-completely-different-secret-32-bytes!!"));
    }

    @Test
    @DisplayName("Service refuses to start with a secret shorter than 32 bytes")
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 24));
    }

    @Test
    @DisplayName("Service refuses to start with a token lifetime under 1 hour")
    void rejectsNonPositiveLifetime() {
        assertThrows(IllegalStateException.class, () -> new JwtService(TEST_SECRET, 0));
        assertThrows(IllegalStateException.class, () -> new JwtService(TEST_SECRET, -5));
    }
}
