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

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }

    public String getBackText() {
        return backText;
    }

    public void setBackText(String backText) {
        this.backText = backText;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
