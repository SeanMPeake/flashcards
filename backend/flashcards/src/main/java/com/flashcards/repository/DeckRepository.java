package com.flashcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flashcards.model.Deck;

public interface DeckRepository extends JpaRepository<Deck, Long> {
}