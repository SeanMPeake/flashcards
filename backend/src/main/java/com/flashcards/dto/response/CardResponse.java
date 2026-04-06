package com.flashcards.dto.response;

public class CardResponse {

    private Long id;
    private String frontText;
    private String backText;
    private Integer priority;

    public CardResponse() {
    }

    public CardResponse(Long id, String frontText, String backText, Integer priority) {
        this.id = id;
        this.frontText = frontText;
        this.backText = backText;
        this.priority = priority;
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
}
