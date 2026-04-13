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
    public StudyStartResponse startSession(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new DeckNotFoundException(deckId);
        }

        List<Card> cards = cardRepository.findByDeckId(deckId);

        if (cards.isEmpty()) {
            throw new EmptyDeckException(deckId);
        }

        StudySession session = sessionManager.createSession(deckId, cards);
        return new StudyStartResponse(buildResponse(session), session.getDeckSize());
    }

    // Reschedules the card just viewed, persists its new priority to the database,
    // then returns the next card from the heap.
    //
    // If no session exists (e.g. after a server restart), the session is rebuilt
    // from the database before continuing. Priority updates from the lost session
    // will already have been persisted, so the heap reflects current state.
    @Transactional
    @Override
    public CardResponse nextCard(Long deckId, NextCardRequest request) {
        StudySession session = sessionManager.getSession(deckId);

        if (session == null) {
            if (!deckRepository.existsById(deckId)) {
                throw new DeckNotFoundException(deckId);
            }
            List<Card> cards = cardRepository.findByDeckId(deckId);
            if (cards.isEmpty()) {
                throw new EmptyDeckException(deckId);
            }
            session = sessionManager.createSession(deckId, cards);
        }

        // Reschedule the card the user just viewed and persist its updated priority.
        int newPriority = session.reschedule(
                request.getCardId(), request.getPriority(), request.isMarkForReview());

        Card viewed = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getCardId()));
        viewed.setPriority(newPriority);
        cardRepository.save(viewed);

        return buildResponse(session);
    }

    // Extracts the next node from the heap and looks up its full card data from the map.
    // The priority comes from the heap node rather than the card entity because the
    // heap may hold an updated value that has not yet been flushed to the database.
    private CardResponse buildResponse(StudySession session) {
        MinHeap.HeapNode node = session.nextNode();
        Card card = session.findCard(node.cardId);
        if (card == null) throw new IllegalStateException("Card not found in session map for id: " + node.cardId);
        return new CardResponse(card.getId(), card.getFrontText(), card.getBackText(), node.priority);
    }
}
