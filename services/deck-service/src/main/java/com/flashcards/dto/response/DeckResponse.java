package com.flashcards.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class DeckResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int cardCount;
    private List<CardResponse> cards;

    public DeckResponse() {
    }

    public DeckResponse(Long id, String name, String description, LocalDateTime createdAt,
                        LocalDateTime updatedAt, int cardCount, List<CardResponse> cards) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getCardCount() {
        return cardCount;
    }

    public List<CardResponse> getCards() {
        return cards;
    }
}
