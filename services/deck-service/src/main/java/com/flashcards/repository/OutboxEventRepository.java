package com.flashcards.repository;

import com.flashcards.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // The oldest waiting events, so they are sent in the order they happened.
    // Capped so one round after a long outage stays a bounded amount of work.
    List<OutboxEvent> findTop100ByOrderByIdAsc();
}
