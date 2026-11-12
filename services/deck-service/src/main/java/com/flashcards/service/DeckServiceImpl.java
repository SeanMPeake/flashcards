package com.flashcards.service;

import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Handles deck read operations and maps JPA entities into DTOs
// so controllers do not return persistence models directly.
@Service
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;

    public DeckServiceImpl(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    // readOnly = true signals that no writes will occur, allowing the
    // database to skip locking overhead and preventing accidental persistence.
    @Transactional(readOnly = true)
    @Override
    public List<DeckSummaryResponse> getDecks(Long userId) {
        return deckRepository.findDeckSummaries(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public DeckResponse getDeckById(Long userId, Long id) {
        return toResponse(requireOwnedDeck(userId, id));
    }

    @Transactional
    @Override
    public DeckResponse createDeck(Long userId, CreateDeckRequest request) {
        Deck deck = new Deck(userId, request.getName(), request.getDescription());

        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    @Override
    public DeckResponse updateDeck(Long userId, Long id, UpdateDeckRequest request) {
        Deck deck = requireOwnedDeck(userId, id);

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());

        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    @Override
    public void deleteDeck(Long userId, Long id) {
        if (!deckRepository.existsByIdAndOwnerUserId(id, userId)) {
            throw new DeckNotFoundException(id);
        }
        deckRepository.deleteById(id);
    }

    // Loads a deck only if this user owns it. Another user's deck is reported
    // as not found (404), so its existence is never revealed.
    private Deck requireOwnedDeck(Long userId, Long id) {
        return deckRepository.findByIdAndOwnerUserId(id, userId)
                .orElseThrow(() -> new DeckNotFoundException(id));
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
                card.getPriority()
        );
    }
}
