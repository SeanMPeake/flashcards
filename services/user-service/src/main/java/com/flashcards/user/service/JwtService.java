package com.flashcards.user.service;

import com.flashcards.common.security.JwtKeys;
import com.flashcards.user.model.User;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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
        // Built by the same shared method the verifier uses, so signer and verifier
        // can never disagree on the key. Refuses a missing or too-short secret.
        this.key = JwtKeys.fromSecret(secret);

        // A lifetime of 0 or less would issue tokens that are already expired,
        // logging every user out at once; refuse to start instead.
        if (expirationHours < 1) {
            throw new IllegalStateException("jwt.expiration-hours must be at least 1");
        }
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
