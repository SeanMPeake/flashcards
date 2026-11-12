package com.flashcards.security;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.common.security.JwtVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// Unit test — runs the filter against fake requests. MockFilterChain records
// whether the request was allowed through to the controllers.
class JwtAuthFilterTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long!";

    private final JwtAuthFilter filter = new JwtAuthFilter(new JwtVerifier(SECRET));

    // A token like the user service issues, for user 7, valid for one hour.
    private String validToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("7")
                .claim("username", "SeanPeake")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private MockHttpServletRequest requestWithAuthorization(String headerValue) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/decks");
        if (headerValue != null) {
            request.addHeader("Authorization", headerValue);
        }
        return request;
    }

    @Test
    @DisplayName("A valid token lets the request through with the user attached")
    void allowsValidToken() throws Exception {
        MockHttpServletRequest request = requestWithAuthorization("Bearer " + validToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest(), "request should reach the controllers");
        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(JwtAuthFilter.AUTHENTICATED_USER);
        assertEquals(7L, user.userId());
        assertEquals("SeanPeake", user.username());
    }

    @Test
    @DisplayName("A request with no token is stopped with 401")
    void rejectsMissingToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithAuthorization(null), response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest(), "request must not reach the controllers");
        assertTrue(response.getContentAsString().contains("Invalid or expired token"));
    }

    @Test
    @DisplayName("A header that isn't 'Bearer <token>' is stopped with 401")
    void rejectsWrongScheme() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithAuthorization("Basic " + validToken()), response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    @DisplayName("An invalid token is stopped with 401")
    void rejectsInvalidToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithAuthorization("Bearer not-a-real-token"), response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }
}
