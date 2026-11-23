package com.flashcards.repository;

import com.flashcards.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface CardRepository extends JpaRepository<Card, Long> {

    // The highest priority in a deck (0 for an empty deck). A new card is placed
    // just after it, so it goes to the back of the study queue even in a deck whose
    // priorities have grown through study.
    @Query("SELECT COALESCE(MAX(c.priority), 0) FROM Card c WHERE c.deck.id = :deckId")
    int findMaxPriority(@Param("deckId") Long deckId);

    // Raises one card's priority, but only if the card is in the given deck and the
    // new value is higher than the stored one. The database checks and writes in a
    // single atomic statement, so two saves arriving at the same moment can never
    // lower a priority (a read-then-write in Java could). Returns the rows changed (0 or 1).
    @Modifying
    @Query("""
        UPDATE Card c
        SET c.priority = :priority, c.updatedAt = :now
        WHERE c.id = :cardId AND c.deck.id = :deckId AND c.priority < :priority
    """)
    int raisePriority(@Param("deckId") Long deckId,
                      @Param("cardId") Long cardId,
                      @Param("priority") int priority,
                      @Param("now") Instant now);
}
