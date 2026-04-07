package com.flashcards.controller;

import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;
import com.flashcards.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // Called when the user selects a deck in study mode.
    // Builds a fresh session and returns the first card.
    @GetMapping("/{deckId}/start")
    public ResponseEntity<StudyStartResponse> startSession(@PathVariable Long deckId) {
        return ResponseEntity.ok(studyService.startSession(deckId));
    }

    // Called each time the user advances to the next card.
    // Reschedules the card just viewed, then returns the next card due.
    @PostMapping("/{deckId}/next")
    public ResponseEntity<CardResponse> nextCard(
            @PathVariable Long deckId,
            @Valid @RequestBody NextCardRequest request) {
        return ResponseEntity.ok(studyService.nextCard(deckId, request));
    }
}
