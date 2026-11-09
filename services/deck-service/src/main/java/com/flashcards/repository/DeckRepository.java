package com.flashcards.repository;

import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    // Returns lightweight deck summaries directly from the database.
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
        GROUP BY d.id, d.name, d.description
        ORDER BY d.id
    """)
    List<DeckSummaryResponse> findDeckSummaries();
}
