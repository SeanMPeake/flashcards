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

        // New cards are assigned priority = current deck size + 1, giving each card
        // a unique starting value and placing it at the back of the scheduling queue.
        int priority = cardRepository.countByDeckId(deckId) + 1;
        Card card = new Card(deck, request.getFrontText(), request.getBackText(), priority);

        return toResponse(cardRepository.save(card));
    }

    @Transactional
    @Override
    public CardResponse updateCard(Long deckId, Long cardId, UpdateCardRequest request) {
        Card card = requireCardInDeck(deckId, cardId);
        card.setFrontText(request.getFrontText());
        card.setBackText(request.getBackText());
        
        return toResponse(cardRepository.save(card));
    }

    @Transactional
    @Override
    public void deleteCard(Long deckId, Long cardId) {
        requireCardInDeck(deckId, cardId);
        cardRepository.deleteById(cardId);
    }

    // Validates that the deck exists, the card exists, and the card belongs to that deck.
    // Cards that exist under a different deck are treated as not found — the client
    // requested the card in the context of the wrong deck.
    private Card requireCardInDeck(Long deckId, Long cardId) {
        if (!deckRepository.existsById(deckId)) {
            throw new DeckNotFoundException(deckId);
        }
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getDeck().getId().equals(deckId)) {
            throw new CardNotFoundException(cardId);
        }
        return card;
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getFrontText(),
                card.getBackText(),
                card.getPriority()
        );
    }
}
