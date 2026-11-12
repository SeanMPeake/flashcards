package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RequestAttribute reads the logged-in user that JwtAuthFilter attached to the
// request after checking the token. Each call is scoped to that user's decks.
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public ResponseEntity<List<DeckSummaryResponse>> getDecks(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user) {
        return ResponseEntity.ok(deckService.getDecks(user.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeckById(user.userId(), id));
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @Valid @RequestBody CreateDeckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(user.userId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(user.userId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long id) {
        deckService.deleteDeck(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
