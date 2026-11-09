package com.flashcards.exception;

public class EmptyDeckException extends RuntimeException {

    public EmptyDeckException(Long deckId) {
        super("Deck " + deckId + " has no cards to study.");
    }
}
