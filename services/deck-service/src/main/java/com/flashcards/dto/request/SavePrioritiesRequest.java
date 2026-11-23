package com.flashcards.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

// A batch of study progress from a session running in the browser: the new card
// priorities, plus what the batch covered (for the StudyProgressSaved event). The
// browser sends one every few cards and when the user leaves study, e.g.
// {"batchId":"...","sessionId":"...","cardsReviewed":5,"cardsFlagged":2,
//  "priorities":[{"cardId":4,"priority":17},{"cardId":9,"priority":23}]}
public class SavePrioritiesRequest {

    // Fixed for this batch, including when the browser retries it; becomes the
    // event's eventId so consumers can skip a batch they've already processed.
    @NotNull
    private UUID batchId;

    // One study session: a single opening of a deck.
    @NotNull
    private UUID sessionId;

    // Cards reviewed and flagged for review in this batch.
    @NotNull
    @PositiveOrZero
    @Max(500)
    private Integer cardsReviewed;

    @NotNull
    @PositiveOrZero
    private Integer cardsFlagged;

    // @NotNull on the element type rejects null entries ([null]); @Valid checks each
    // entry's own rules. The size cap keeps one request bounded.
    @NotEmpty
    @Size(max = 500)
    private List<@NotNull @Valid PriorityUpdate> priorities;

    public SavePrioritiesRequest() {
    }

    public SavePrioritiesRequest(UUID batchId, UUID sessionId, Integer cardsReviewed,
                                 Integer cardsFlagged, List<PriorityUpdate> priorities) {
        this.batchId = batchId;
        this.sessionId = sessionId;
        this.cardsReviewed = cardsReviewed;
        this.cardsFlagged = cardsFlagged;
        this.priorities = priorities;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getCardsReviewed() {
        return cardsReviewed;
    }

    public void setCardsReviewed(Integer cardsReviewed) {
        this.cardsReviewed = cardsReviewed;
    }

    public Integer getCardsFlagged() {
        return cardsFlagged;
    }

    public void setCardsFlagged(Integer cardsFlagged) {
        this.cardsFlagged = cardsFlagged;
    }

    public List<PriorityUpdate> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<PriorityUpdate> priorities) {
        this.priorities = priorities;
    }

    public static class PriorityUpdate {

        @NotNull
        private Long cardId;

        // Priorities start at 1 and only grow, so negatives are invalid. The upper bound
        // is far above any real value (each review adds the deck size) but keeps
        // later "priority + deckSize" arithmetic well clear of integer overflow.
        @NotNull
        @PositiveOrZero
        @Max(1_000_000_000)
        private Integer priority;

        public PriorityUpdate() {
        }

        public PriorityUpdate(Long cardId, Integer priority) {
            this.cardId = cardId;
            this.priority = priority;
        }

        public Long getCardId() {
            return cardId;
        }

        public void setCardId(Long cardId) {
            this.cardId = cardId;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }
}
