package com.flashcards.service;

import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;

import java.util.List;

public interface DeckService {

    List<DeckSummaryResponse> getDecks();

    DeckResponse getDeckById(Long id);

    DeckResponse createDeck(CreateDeckRequest request);

    DeckResponse updateDeck(Long id, UpdateDeckRequest request);

    void deleteDeck(Long id);
}
