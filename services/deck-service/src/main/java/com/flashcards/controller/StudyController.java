package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Study itself runs in the browser (it loads the deck with GET /api/decks/{id} and
// schedules cards locally). This endpoint only saves the resulting priorities.
// @RequestAttribute reads the logged-in user that JwtAuthFilter attached to the request.
@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // Called by the browser every few cards and when the user leaves study.
    @PutMapping("/{deckId}/priorities")
    public ResponseEntity<Void> savePriorities(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId,
            @Valid @RequestBody SavePrioritiesRequest request) {
        studyService.savePriorities(user.userId(), deckId, request);
        return ResponseEntity.noContent().build();
    }
}
