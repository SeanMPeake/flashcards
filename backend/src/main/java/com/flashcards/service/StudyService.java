package com.flashcards.service;

import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;

public interface StudyService {

    // Builds a fresh study session for the given deck and returns the first card.
    CardResponse startSession(Long deckId);

    // Reschedules the card just viewed, then returns the next card due.
    CardResponse nextCard(Long deckId, NextCardRequest request);
}
