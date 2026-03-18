package com.flashcards.service;

import com.flashcards.dto.response.DeckResponse;

import java.util.List;

public interface DeckService {

    List<DeckResponse> getDecks();

    DeckResponse getDeckById(Long id);
}
