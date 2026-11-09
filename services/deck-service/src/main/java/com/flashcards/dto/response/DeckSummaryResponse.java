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

    // Used by the JPQL summary query because COUNT(...) is returned as a Long.
    public DeckSummaryResponse(Long id, String name, String description, Long cardCount) {
        this(id, name, description, cardCount == null ? 0 : cardCount.intValue());
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
