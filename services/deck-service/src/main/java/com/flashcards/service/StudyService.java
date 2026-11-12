package com.flashcards.service;

import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;

// Every method takes the id of the logged-in user and only works on that user's decks.
public interface StudyService {

    // Builds a fresh study session for the given deck and returns the first card
    // along with the total number of cards in the deck.
    StudyStartResponse startSession(Long userId, Long deckId);

    // Reschedules the card just viewed, then returns the next card due.
    CardResponse nextCard(Long userId, Long deckId, NextCardRequest request);
}
