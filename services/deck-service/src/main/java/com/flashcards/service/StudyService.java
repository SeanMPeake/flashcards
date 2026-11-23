package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;

// Study runs in the browser: it loads a deck once, schedules the cards itself, and
// sends the new priorities back in batches. The server keeps no study state, so any
// copy of the deck service can handle any request.
public interface StudyService {

    // Saves new priorities for cards in one of the user's own decks.
    void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request);
}
