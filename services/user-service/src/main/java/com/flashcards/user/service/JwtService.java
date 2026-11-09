package com.flashcards.user.service;

import com.flashcards.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

// Creates signed login tokens (JWTs). Tokens are signed with HS256 using a shared
// secret; any service holding the same secret can verify them, and none can alter
// a token without invalidating its signature.
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration lifetime;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-hours}") long expirationHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 requires a key of at least 256 bits; refuse to start with a weaker one.
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.lifetime = Duration.ofHours(expirationHours);
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())          // who: other services read this
                .claim("username", user.getUsername())     // for display
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(key)                             // HS256, chosen from the key size
                .compact();
    }

    public long getLifetimeSeconds() {
        return lifetime.getSeconds();
    }
}
