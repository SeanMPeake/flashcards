package com.flashcards.service;

import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;

public interface CardService {

    CardResponse createCard(Long deckId, CreateCardRequest request);

    CardResponse updateCard(Long deckId, Long cardId, UpdateCardRequest request);

    void deleteCard(Long deckId, Long cardId);
}
