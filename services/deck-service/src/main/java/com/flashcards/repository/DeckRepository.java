package com.flashcards.repository;

import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Every lookup here is scoped to an owner, so a user can only ever reach their own decks.
public interface DeckRepository extends JpaRepository<Deck, Long> {

    // Returns lightweight summaries of one user's decks directly from the database.
    // This avoids loading each deck's full card collection just to calculate card counts.
    @Query("""
        SELECT new com.flashcards.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            d.description,
            COUNT(c)
        )
        FROM Deck d
        LEFT JOIN d.cards c
        WHERE d.ownerUserId = :ownerUserId
        GROUP BY d.id, d.name, d.description
        ORDER BY d.id
    """)
    List<DeckSummaryResponse> findDeckSummaries(@Param("ownerUserId") Long ownerUserId);

    // A deck only if it belongs to this user. Someone else's deck comes back empty,
    // the same as a deck that doesn't exist, so its existence is never revealed.
    Optional<Deck> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    boolean existsByIdAndOwnerUserId(Long id, Long ownerUserId);
}
