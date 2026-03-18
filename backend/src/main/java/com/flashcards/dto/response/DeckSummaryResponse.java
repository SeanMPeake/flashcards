package com.flashcards.dto.response;

public class DeckSummaryResponse {

    private Long id;
    private String name;
    private String description;
    private int cardCount;

    public DeckSummaryResponse() {
    }

    public DeckSummaryResponse(Long id, String name, String description, int cardCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cardCount = cardCount;
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

    public int getCardCount() {
        return cardCount;
    }
}
