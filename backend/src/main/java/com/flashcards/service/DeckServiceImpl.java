package com.flashcards.service;

import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Handles deck read operations and maps JPA entities into DTOs
// so controllers do not return persistence models directly.
@Service
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;

    public DeckServiceImpl(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Override
    public List<DeckSummaryResponse> getDecks() {
        return deckRepository.findDeckSummaries();
    }

    @Override
    public DeckResponse getDeckById(Long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new DeckNotFoundException(id));

        return toResponse(deck);
    }

    // Builds the full deck response used by the single-deck endpoint,
    // including the nested card data needed for study mode.
    private DeckResponse toResponse(Deck deck) {
        List<CardResponse> cards = deck.getCards()
                .stream()
                .map(this::toCardResponse)
                .toList();

        return new DeckResponse(
                deck.getId(),
                deck.getName(),
                deck.getDescription(),
                deck.getCreatedAt(),
                deck.getUpdatedAt(),
                cards.size(),
                cards
        );
    }

    // Centralizes card-to-DTO mapping so card response fields stay consistent.
    private CardResponse toCardResponse(Card card) {
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
