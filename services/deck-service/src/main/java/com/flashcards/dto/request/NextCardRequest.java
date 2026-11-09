package com.flashcards.dto.request;

import jakarta.validation.constraints.NotNull;

public class NextCardRequest {

    @NotNull
    private Long cardId;

    private int priority;

    private boolean markForReview;

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isMarkForReview() {
        return markForReview;
    }

    public void setMarkForReview(boolean markForReview) {
        this.markForReview = markForReview;
    }
}
