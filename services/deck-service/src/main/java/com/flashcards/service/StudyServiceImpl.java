package com.flashcards.service;

import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.messaging.StudyEventPublisher;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Service
public class StudyServiceImpl implements StudyService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final StudyEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public StudyServiceImpl(CardRepository cardRepository, DeckRepository deckRepository,
                            StudyEventPublisher eventPublisher, TransactionTemplate transactionTemplate) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
    }

    // Saves a batch of study progress, then publishes a StudyProgressSaved event.
    //
    // Saving: each entry is applied with a single conditional UPDATE (see
    // CardRepository.raisePriority), which only changes a card that is in this deck
    // and only raises its priority:
    // - Card ids not in this deck change nothing, so ids from other decks are never
    //   touched, and a card deleted mid-study can't make the save fail forever.
    // - A priority is never lowered. Each review only adds to a card's priority, so
    //   a lower value is always an older one: a late batch, a retried save, or a
    //   second tab on the same deck. Keeping the highest value lets them all merge
    //   correctly, and a card listed twice keeps its highest value.
    // - The database compares and writes in one atomic step, so two saves arriving
    //   at the same moment can't lower a priority either.
    //
    // Publishing happens only after the save has committed, so an event never
    // describes progress that was rolled back. If publishing fails, the request fails
    // (503) and the browser retries the same batch: the save repeats harmlessly and the
    // event is sent again with the same eventId, which consumers skip if they already
    // have it. That gives at-least-once delivery without an outbox table. (At scale: a
    // transactional outbox, so the event is stored in the same transaction as the save.)
    @Override
    public void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request) {
        if (request.getCardsFlagged() > request.getCardsReviewed()) {
            throw new IllegalArgumentException("cardsFlagged cannot exceed cardsReviewed");
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
                // Another user's deck is reported as not found (404), so its existence is never revealed.
                throw new DeckNotFoundException(deckId);
            }

            Instant now = Instant.now();
            for (SavePrioritiesRequest.PriorityUpdate update : request.getPriorities()) {
                cardRepository.raisePriority(deckId, update.getCardId(), update.getPriority(), now);
            }
        });

        eventPublisher.publish(new StudyProgressSaved(
                request.getBatchId().toString(),
                userId,
                deckId,
                request.getSessionId().toString(),
                request.getCardsReviewed(),
                request.getCardsFlagged(),
                Instant.now()));
    }
}
