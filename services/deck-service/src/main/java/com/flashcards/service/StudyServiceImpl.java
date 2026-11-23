package com.flashcards.service;

import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Service
public class StudyServiceImpl implements StudyService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final OutboxEventRepository outboxRepository;
    private final JsonMapper jsonMapper;

    public StudyServiceImpl(CardRepository cardRepository, DeckRepository deckRepository,
                            OutboxEventRepository outboxRepository, JsonMapper jsonMapper) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }

    // Saves a batch of study progress and records a StudyProgressSaved event for it.
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
    // The event: it is stored in the outbox table in the same transaction as the
    // priorities, so both are saved or neither is. OutboxRelay sends it to RabbitMQ
    // in the background, so the broker being down never affects saving or studying.
    // The eventId is the user id plus the browser's batchId: if the browser re-sends a
    // batch, the event is stored again with the same id, and consumers skip the repeat.
    // Including the user id means one user's batch ids can never collide with another's.
    @Override
    @Transactional
    public void savePriorities(Long userId, Long deckId, SavePrioritiesRequest request) {
        if (request.getCardsFlagged() > request.getCardsReviewed()) {
            throw new IllegalArgumentException("cardsFlagged cannot exceed cardsReviewed");
        }
        if (!deckRepository.existsByIdAndOwnerUserId(deckId, userId)) {
            // Another user's deck is reported as not found (404), so its existence is never revealed.
            throw new DeckNotFoundException(deckId);
        }

        Instant now = Instant.now();
        for (SavePrioritiesRequest.PriorityUpdate update : request.getPriorities()) {
            cardRepository.raisePriority(deckId, update.getCardId(), update.getPriority(), now);
        }

        StudyProgressSaved event = new StudyProgressSaved(
                userId + ":" + request.getBatchId(),
                userId,
                deckId,
                request.getSessionId().toString(),
                request.getCardsReviewed(),
                request.getCardsFlagged(),
                now);
        outboxRepository.save(new OutboxEvent(event.eventId(), jsonMapper.writeValueAsString(event)));
    }
}
