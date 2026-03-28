package com.flashcards.service;

import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.exception.CardNotFoundException;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public CardServiceImpl(CardRepository cardRepository, DeckRepository deckRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional
    @Override
    public CardResponse createCard(Long deckId, CreateCardRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));

        // New cards start with a default priority of 0. The scheduling
        // algorithm will assign and update priority during study sessions.
        Card card = new Card(deck, request.getFrontText(), request.getBackText(), 0);
        return toResponse(cardRepository.save(card));
    }

    @Transactional
    @Override
    public CardResponse updateCard(Long deckId, Long cardId, UpdateCardRequest request) {
        if (!deckRepository.existsById(deckId)) {
            throw new DeckNotFoundException(deckId);
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        // Card exists but belongs to a different deck — treat as not found
        // since the client requested it in the context of the wrong deck.
        if (!card.getDeck().getId().equals(deckId)) {
            throw new CardNotFoundException(cardId);
        }

        card.setFrontText(request.getFrontText());
        card.setBackText(request.getBackText());

        return toResponse(cardRepository.save(card));
    }

    @Transactional
    @Override
    public void deleteCard(Long deckId, Long cardId) {
        if (!deckRepository.existsById(deckId)) {
            throw new DeckNotFoundException(deckId);
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        // Card exists but belongs to a different deck — treat as not found
        // since the client requested it in the context of the wrong deck.
        if (!card.getDeck().getId().equals(deckId)) {
            throw new CardNotFoundException(cardId);
        }

        cardRepository.deleteById(cardId);
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getFrontText(),
                card.getBackText(),
                card.getPriority(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
