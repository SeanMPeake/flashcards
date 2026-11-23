package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
    // Each entry is applied with a single conditional UPDATE (see
    // CardRepository.raisePriority), which only changes a card that is in this deck
    // and only raises its priority:
    // - Card ids not in this deck change nothing, so ids from other decks are never
    //   touched, and a card deleted mid-study can't make the save fail forever.
    // - A priority is never lowered. Each review only adds to a card's priority, so
    //   a lower value is always an older one: a late batch, a retried save, or a
    //   second tab on the same deck. Keeping the highest value lets them all merge
    //   correctly without timestamps, and a card listed twice keeps its highest value.
    // - The database compares and writes in one atomic step, so two saves arriving
    //   at the same moment can't lower a priority either.
    @Transactional
    @Override
    public void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request) {
        if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
            // Another user's deck is reported as not found (404), so its existence is never revealed.
            throw new DeckNotFoundException(deckId);
        }

        Instant now = Instant.now();
        for (SavePrioritiesRequest.PriorityUpdate update : request.getPriorities()) {
            cardRepository.raisePriority(deckId, update.getCardId(), update.getPriority(), now);
        }
    }
}
