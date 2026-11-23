package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Transactional
    @Override
    public void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request) {
        if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
            // Another user's deck is reported as not found (404), so its existence is never revealed.
            throw new DeckNotFoundException(deckId);
        }

        Map<Long, Card> cardsInDeck = cardRepository.findByDeckId(deckId).stream()
                .collect(Collectors.toMap(Card::getId, Function.identity()));

        List<Card> changed = new ArrayList<>();
        for (SavePrioritiesRequest.PriorityUpdate update : request.getPriorities()) {
            Card card = cardsInDeck.get(update.getCardId());
            if (card != null) {
                card.setPriority(update.getPriority());
                changed.add(card);
            }
        }
        cardRepository.saveAll(changed);
    }
}
