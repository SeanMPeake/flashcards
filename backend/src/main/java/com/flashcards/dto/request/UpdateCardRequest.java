package com.flashcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateCardRequest {

    @NotBlank
    private String frontText;

    @NotBlank
    private String backText;

    public UpdateCardRequest() {
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
}
