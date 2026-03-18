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

    public void setId(Long id) {
        this.id = id;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public List<CardResponse> getCards() {
        return cards;
    }

    public void setCards(List<CardResponse> cards) {
        this.cards = cards;
    }
}
