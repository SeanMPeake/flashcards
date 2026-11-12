package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RequestAttribute reads the logged-in user that JwtAuthFilter attached to the
// request; study only works on that user's own decks.
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
    public ResponseEntity<StudyStartResponse> startSession(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId) {
        return ResponseEntity.ok(studyService.startSession(user.userId(), deckId));
    }

    // Called each time the user advances to the next card.
    // Reschedules the card just viewed, then returns the next card due.
    @PostMapping("/{deckId}/next")
    public ResponseEntity<CardResponse> nextCard(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId,
            @Valid @RequestBody NextCardRequest request) {

        return ResponseEntity.ok(studyService.nextCard(user.userId(), deckId, request));
    }
}
