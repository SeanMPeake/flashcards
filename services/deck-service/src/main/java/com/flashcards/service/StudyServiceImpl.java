package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudyServiceImpl implements StudyService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public StudyServiceImpl(CardRepository cardRepository, DeckRepository deckRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
    }

    // Saves a batch of priorities from the browser in one transaction.
    //
    // Only cards in this deck, which this user owns, are ever changed. Card ids that
    // aren't in the deck are skipped rather than failing the whole batch: a card
    // deleted in another tab mid-study must not make the save fail forever (the
    // browser would keep retrying it), and ids from other decks are never touched.
    //
    // A priority is never lowered. Each review only adds to a card's priority, so a
    // lower value is always an older one: a batch that arrives late, a retried save,
    // or a second tab studying the same deck. Keeping the highest value lets all of
    // them merge correctly without timestamps (and the same rule settles a card
    // listed twice in one batch).
    @Transactional
    @Override
    public void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request) {
        if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
            // Another user's deck is reported as not found (404), so its existence is never revealed.
            throw new DeckNotFoundException(deckId);
        }

        // Load only the cards named in the batch, then keep the ones in this deck.
        List<Long> requestedIds = request.getPriorities().stream()
                .map(SavePrioritiesRequest.PriorityUpdate::getCardId)
                .toList();
        Map<Long, Card> cardsInDeck = cardRepository.findAllById(requestedIds).stream()
                .filter(card -> card.getDeck().getId().equals(deckId))
                .collect(Collectors.toMap(Card::getId, Function.identity()));

        Set<Card> changed = new LinkedHashSet<>();
        for (SavePrioritiesRequest.PriorityUpdate update : request.getPriorities()) {
            Card card = cardsInDeck.get(update.getCardId());
            if (card != null && update.getPriority() > card.getPriority()) {
                card.setPriority(update.getPriority());
                changed.add(card);
            }
        }
        cardRepository.saveAll(changed);
    }
}
