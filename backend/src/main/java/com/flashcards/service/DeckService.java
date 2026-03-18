package com.flashcards.service;

import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;

import java.util.List;

public interface DeckService {

    List<DeckSummaryResponse> getDecks();

    DeckResponse getDeckById(Long id);
}
