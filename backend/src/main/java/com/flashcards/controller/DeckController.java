package com.flashcards.controller;

import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public ResponseEntity<List<DeckSummaryResponse>> getDecks() {
        return ResponseEntity.ok(deckService.getDecks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeckById(id));
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@Valid @RequestBody CreateDeckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long id, @Valid @RequestBody UpdateDeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
