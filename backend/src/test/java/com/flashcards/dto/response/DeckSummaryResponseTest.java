package com.flashcards.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Unit Test — verifies DeckSummaryResponse constructor behavior in isolation.
class DeckSummaryResponseTest {

    @Test
    @DisplayName("Long card count constructor converts JPQL count values to int")
    void longCardCountConstructorConvertsToInt() {
        DeckSummaryResponse response = new DeckSummaryResponse(
                1L,
                "Java Basics",
                "Core Java review cards",
                2L
        );

        assertEquals(1L, response.getId());
        assertEquals("Java Basics", response.getName());
        assertEquals("Core Java review cards", response.getDescription());
        assertEquals(2, response.getCardCount());
    }

    @Test
    @DisplayName("Long card count constructor defaults null counts to zero")
    void longCardCountConstructorDefaultsNullToZero() {
        DeckSummaryResponse response = new DeckSummaryResponse(
                2L,
                "Empty Deck",
                "No cards yet",
                (Long) null
        );

        assertEquals(0, response.getCardCount());
    }
}
