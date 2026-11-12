package com.flashcards.service;

import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;

// Every method takes the id of the logged-in user; cards are reached only
// through a deck that user owns.
public interface CardService {

    CardResponse createCard(Long userId, Long deckId, CreateCardRequest request);

    CardResponse updateCard(Long userId, Long deckId, Long cardId, UpdateCardRequest request);

    void deleteCard(Long userId, Long deckId, Long cardId);
}
