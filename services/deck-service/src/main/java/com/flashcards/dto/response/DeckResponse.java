package com.flashcards.dto.response;

import java.time.Instant;
import java.util.List;

public class DeckResponse {

    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private int cardCount;
    private List<CardResponse> cards;

    public DeckResponse() {
    }

    public DeckResponse(Long id, String name, String description, Instant createdAt,
                        Instant updatedAt, int cardCount, List<CardResponse> cards) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cardCount = cardCount;
        this.cards = cards;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getCardCount() {
        return cardCount;
    }

    public List<CardResponse> getCards() {
        return cards;
    }
}
