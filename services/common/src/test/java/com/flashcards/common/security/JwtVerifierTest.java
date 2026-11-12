package com.flashcards.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// Unit test — builds real tokens the same way the user service does, then checks
// that the verifier accepts only genuine, unexpired, untampered ones.
class JwtVerifierTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long!";
    private static final String OTHER_SECRET = "a-completely-different-secret-32-bytes!!";

    private final JwtVerifier verifier = new JwtVerifier(SECRET);

    // A token like the user service issues, signed with the given secret and
    // expiring at the given moment.
    private String token(String secret, String subject, Instant expiresAt) {
        return Jwts.builder()
                .subject(subject)
                .claim("username", "SeanPeake")
                .issuedAt(Date.from(expiresAt.minus(Duration.ofHours(24))))
                .expiration(Date.from(expiresAt))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private Instant inOneHour() {
        return Instant.now().plus(Duration.ofHours(1));
    }

    @Test
    @DisplayName("A valid token returns its user id and username")
    void acceptsValidToken() {
        AuthenticatedUser user = verifier.verify(token(SECRET, "7", inOneHour()));

        assertEquals(7L, user.userId());
        assertEquals("SeanPeake", user.username());
    }

    @Test
    @DisplayName("An expired token is rejected")
    void rejectsExpiredToken() {
        String expired = token(SECRET, "7", Instant.now().minus(Duration.ofMinutes(5)));

        assertThrows(InvalidTokenException.class, () -> verifier.verify(expired));
    }

    @Test
    @DisplayName("A token signed with a different secret is rejected")
    void rejectsTokenFromAnotherSecret() {
        String forged = token(OTHER_SECRET, "7", inOneHour());

        assertThrows(InvalidTokenException.class, () -> verifier.verify(forged));
    }

    @Test
    @DisplayName("A token whose contents were changed is rejected")
    void rejectsTamperedToken() {
        // Rewrite the payload to claim user 1 while keeping the original signature.
        String[] parts = token(SECRET, "7", inOneHour()).split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String tamperedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.replace("\"sub\":\"7\"", "\"sub\":\"1\"")
                        .getBytes(StandardCharsets.UTF_8));
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThrows(InvalidTokenException.class, () -> verifier.verify(tampered));
    }

    @Test
    @DisplayName("Text that is not a token is rejected")
    void rejectsGarbage() {
        assertThrows(InvalidTokenException.class, () -> verifier.verify("not-a-token"));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(""));
    }

    @Test
    @DisplayName("A token whose subject is not a number is rejected")
    void rejectsNonNumericSubject() {
        String odd = token(SECRET, "not-a-number", inOneHour());

        assertThrows(InvalidTokenException.class, () -> verifier.verify(odd));
    }

    @Test
    @DisplayName("The verifier refuses a secret shorter than 32 bytes")
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtVerifier("too-short"));
    }
}
