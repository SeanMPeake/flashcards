package com.flashcards.dto.response;

import java.time.LocalDateTime;

public class CardResponse {

    private Long id;
    private String frontText;
    private String backText;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CardResponse() {
    }

    public CardResponse(Long id, String frontText, String backText, Integer priority,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.frontText = frontText;
        this.backText = backText;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFrontText() {
        return frontText;
    }

    public String getBackText() {
        return backText;
    }

    public Integer getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
