package com.flashcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateDeckRequest {

    @NotBlank
    private String name;
    private String description;

    public UpdateDeckRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
