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

@Service
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;

    public DeckServiceImpl(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Override
    public List<DeckSummaryResponse> getDecks() {
        return deckRepository.findAll()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    public DeckResponse getDeckById(Long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new DeckNotFoundException(id));

        return toResponse(deck);
    }

    private DeckResponse toResponse(Deck deck) {
        List<CardResponse> cards = deck.getCards() == null
                ? List.of()
                : deck.getCards()
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

    private DeckSummaryResponse toSummaryResponse(Deck deck) {
        int cardCount = deck.getCards() == null ? 0 : deck.getCards().size();

        return new DeckSummaryResponse(
                deck.getId(),
                deck.getName(),
                deck.getDescription(),
                cardCount
        );
    }

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
