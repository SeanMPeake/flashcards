package com.flashcards.security;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.common.security.InvalidTokenException;
import com.flashcards.common.security.JwtVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Runs before every /api request reaches a controller. A request must carry a
// valid login token ("Authorization: Bearer <token>"); the token is checked here
// with the shared secret, so the deck service never calls the user service.
//
// Valid token: the AuthenticatedUser is attached to the request under
// AUTHENTICATED_USER for controllers to read, and the request continues.
// Missing or invalid token: the request stops here with a 401.
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER = "authenticatedUser";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtVerifier jwtVerifier;

    public JwtAuthFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            reject(response);
            return;
        }

        try {
            AuthenticatedUser user = jwtVerifier.verify(header.substring(BEARER_PREFIX.length()));
            request.setAttribute(AUTHENTICATED_USER, user);
        } catch (InvalidTokenException e) {
            reject(response);
            return;
        }

        chain.doFilter(request, response);
    }

    // Same {status, message} shape as every other error response from this service.
    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"message\":\"Invalid or expired token\"}");
    }
}
