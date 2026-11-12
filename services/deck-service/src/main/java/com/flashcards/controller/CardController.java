package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RequestAttribute reads the logged-in user that JwtAuthFilter attached to the
// request; cards are only reachable through a deck that user owns.
@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId,
            @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(user.userId(), deckId, request));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(user.userId(), deckId, cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @RequestAttribute(JwtAuthFilter.AUTHENTICATED_USER) AuthenticatedUser user,
            @PathVariable Long deckId,
            @PathVariable Long cardId) {
        cardService.deleteCard(user.userId(), deckId, cardId);
        return ResponseEntity.noContent().build();
    }
}
