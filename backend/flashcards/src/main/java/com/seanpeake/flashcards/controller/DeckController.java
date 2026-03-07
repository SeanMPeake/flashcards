package com.seanpeake.flashcards.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DeckController {

    @GetMapping("/api/decks")
    public List<Map<String, Object>> getDecks() {
        return List.of(
                Map.of(
                        "id", 1,
                        "name", "Java Basics",
                        "cardCount", 10
                ),
                Map.of(
                        "id", 2,
                        "name", "SDLC Terms",
                        "cardCount", 4
                )
        );
    }
}