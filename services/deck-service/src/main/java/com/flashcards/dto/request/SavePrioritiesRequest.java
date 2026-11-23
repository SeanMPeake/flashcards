package com.flashcards.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

// A batch of new card priorities from a study session running in the browser.
// The browser schedules cards itself and sends the changed priorities every few
// cards and when the user leaves study, e.g.
// {"priorities":[{"cardId":4,"priority":17},{"cardId":9,"priority":23}]}
public class SavePrioritiesRequest {

    // @NotNull on the element type rejects null entries ([null]); @Valid checks each
    // entry's own rules. The size cap keeps one request bounded.
    @NotEmpty
    @Size(max = 500)
    private List<@NotNull @Valid PriorityUpdate> priorities;

    public SavePrioritiesRequest() {
    }

    public SavePrioritiesRequest(List<PriorityUpdate> priorities) {
        this.priorities = priorities;
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
