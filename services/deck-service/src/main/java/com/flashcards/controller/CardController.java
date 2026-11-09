package com.flashcards.controller;

import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId, @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(deckId, request));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long deckId, @PathVariable Long cardId, @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(deckId, cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long deckId, @PathVariable Long cardId) {
        cardService.deleteCard(deckId, cardId);
        return ResponseEntity.noContent().build();
    }
}
