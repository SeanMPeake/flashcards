package com.flashcards.repository;

import com.flashcards.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    // Loads all cards belonging to a given deck.
    // Used when building a study session to populate the heap and hash map.
    List<Card> findByDeckId(Long deckId);

    // Returns the number of cards in a deck.
    // Used to assign each new card a unique starting priority equal to deck size + 1,
    // placing it at the back of the scheduling queue.
    int countByDeckId(Long deckId);
}