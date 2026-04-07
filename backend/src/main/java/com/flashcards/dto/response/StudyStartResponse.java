package com.flashcards.dto.response;

// Response returned when a study session starts.
// Bundles the first card with the total number of cards in the deck so the
// frontend can initialize the card position display ("X of Y cards seen")
// without a separate request.
public class StudyStartResponse {

    private final CardResponse card;
    private final int totalCards;

    public StudyStartResponse(CardResponse card, int totalCards) {
        this.card = card;
        this.totalCards = totalCards;
    }

    public CardResponse getCard() {
        return card;
    }

    public int getTotalCards() {
        return totalCards;
    }
}
