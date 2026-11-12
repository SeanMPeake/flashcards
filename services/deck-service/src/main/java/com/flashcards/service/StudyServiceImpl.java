package com.flashcards.service;

import com.flashcards.datastructure.MinHeap;
import com.flashcards.datastructure.StudySession;
import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;
import com.flashcards.exception.CardNotFoundException;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EmptyDeckException;
import com.flashcards.model.Card;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyServiceImpl implements StudyService {

    private final StudySessionManager sessionManager;
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public StudyServiceImpl(StudySessionManager sessionManager,
                            CardRepository cardRepository,
                            DeckRepository deckRepository) {
        this.sessionManager = sessionManager;
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
    }

    // Rebuilds the session from the database each time study mode is entered for a deck.
    // This ensures the heap and hash map always reflect current card priorities,
    // avoiding any stale state from a previous session.
    @Transactional(readOnly = true)
    @Override
    public StudyStartResponse startSession(Long userId, Long deckId) {
        requireOwnedDeck(userId, deckId);

        List<Card> cards = cardRepository.findByDeckId(deckId);

        if (cards.isEmpty()) {
            throw new EmptyDeckException(deckId);
        }

        StudySession session = sessionManager.createSession(userId, deckId, cards);

        return new StudyStartResponse(buildResponse(session), session.getDeckSize());
    }

    // Reschedules the card just viewed, persists its new priority to the database,
    // then returns the next card from the heap.
    //
    // Ownership is checked on every call, even when a session already exists, so no
    // one can continue a study session on a deck they don't own.
    //
    // If no session exists (e.g. after a server restart), the session is rebuilt
    // from the database before continuing. Priority updates from the lost session
    // will already have been persisted, so the heap reflects current state.
    @Transactional
    @Override
    public CardResponse nextCard(Long userId, Long deckId, NextCardRequest request) {
        requireOwnedDeck(userId, deckId);

        StudySession session = sessionManager.getSession(userId, deckId);

        if (session == null) {
            List<Card> cards = cardRepository.findByDeckId(deckId);

            if (cards.isEmpty()) {
                throw new EmptyDeckException(deckId);
            }

            session = sessionManager.createSession(userId, deckId, cards);
        }

        // The card id comes from the request, so check it belongs to this deck before
        // changing anything; otherwise a user could alter a card in someone else's deck.
        Card viewed = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getCardId()));
        if (!viewed.getDeck().getId().equals(deckId)) {
            throw new CardNotFoundException(request.getCardId());
        }

        // Reschedule the card the user just viewed and persist its updated priority.
        int newPriority = session.reschedule(
                request.getCardId(), request.getPriority(), request.isMarkForReview());
        viewed.setPriority(newPriority);
        cardRepository.save(viewed);

        return buildResponse(session);
    }

    // Another user's deck is reported as not found (404), so its existence is never revealed.
    private void requireOwnedDeck(Long userId, Long deckId) {
        if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
            throw new DeckNotFoundException(deckId);
        }
    }

    // Extracts the next node from the heap and looks up its full card data from the map.
    // The priority comes from the heap node rather than the card entity because the
    // heap may hold an updated value that has not yet been flushed to the database.
    private CardResponse buildResponse(StudySession session) {
        MinHeap.HeapNode node = session.nextNode();
        Card card = session.findCard(node.cardId);

        if (card == null) {
            throw new IllegalStateException("Card not found in session map for id: " + node.cardId);
        }
        
        return new CardResponse(card.getId(), card.getFrontText(), card.getBackText(), node.priority);
    }
}
