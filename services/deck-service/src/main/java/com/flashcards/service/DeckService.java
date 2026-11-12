package com.flashcards.service;

import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;

import java.util.List;

// Every method takes the id of the logged-in user (from their token) and only
// ever reads or changes that user's own decks.
public interface DeckService {

    List<DeckSummaryResponse> getDecks(Long userId);

    DeckResponse getDeckById(Long userId, Long id);

    DeckResponse createDeck(Long userId, CreateDeckRequest request);

    DeckResponse updateDeck(Long userId, Long id, UpdateDeckRequest request);

    void deleteDeck(Long userId, Long id);
}
