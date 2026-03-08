package com.flashcards.controller;

import com.flashcards.model.Deck;
import com.flashcards.repository.DeckRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeckController {

    private final DeckRepository deckRepository;

    public DeckController(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @GetMapping("/api/decks")
    public List<Deck> getDecks() {
        return deckRepository.findAll();
    }
}