package com.flashcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCardRequest {

    @NotBlank
    private String frontText;

    @NotBlank
    private String backText;

    public CreateCardRequest() {
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
