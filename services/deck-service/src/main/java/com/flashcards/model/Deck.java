package com.flashcards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who owns this deck: the id from their login token. A plain number
    // rather than a foreign key, because the users table lives in the user
    // service's schema, which this service cannot read. Cards belong to a deck,
    // so they are owned through it.
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // A deck owns many cards. The Jackson reference annotation helps prevent
    // infinite recursion if entity objects are serialized directly to JSON.
    // The API now mainly returns DTOs, so DTOs define the response shape,
    // but these annotations still act as a useful safeguard on the entity model.
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Card> cards = new ArrayList<>();

    public Deck() {
    }

    public Deck(Long ownerUserId, String name, String description) {
        if (ownerUserId == null) {
            throw new IllegalArgumentException("ownerUserId must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.description = description;
    }

    // Set timestamps automatically on first save. updatedAt is also kept in sync on any subsequent update.
    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Card> getCards() {
        return cards;
    }
}
