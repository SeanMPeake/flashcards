package com.flashcards.user.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The username exactly as the user typed it, used for display.
    @Column(nullable = false, length = 20)
    private String username;

    // Lowercased copy of the username. Uniqueness and lookups use this column,
    // so "SeanPeake" and "seanpeake" count as the same name.
    @Column(name = "username_normalized", nullable = false, unique = true, length = 20)
    private String usernameNormalized;

    // BCrypt hash of the password. The raw password is never stored.
    // BCrypt hashes are always 60 characters long.
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String usernameNormalized, String passwordHash) {
        if (username == null) {
            throw new IllegalArgumentException("username must not be null");
        }
        if (usernameNormalized == null) {
            throw new IllegalArgumentException("usernameNormalized must not be null");
        }
        if (passwordHash == null) {
            throw new IllegalArgumentException("passwordHash must not be null");
        }
        this.username = username;
        this.usernameNormalized = usernameNormalized;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameNormalized() {
        return usernameNormalized;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
