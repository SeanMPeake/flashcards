package com.flashcards.model;

import jakarta.persistence.*;
import java.time.Instant;

// An event waiting to be sent to RabbitMQ (the "transactional outbox").
//
// The row is written in the same database transaction as the change it describes,
// so the two are saved together or not at all. OutboxRelay sends waiting rows to
// the broker in the background and deletes each one once it has been sent. While
// the broker is down, rows simply wait here; nothing the user does is affected.
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The event's own id, which consumers use to skip an event they already have.
    @Column(name = "event_id", nullable = false)
    private String eventId;

    // The event as JSON, exactly as it will be sent.
    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OutboxEvent() {
    }

    public OutboxEvent(String eventId, String payload) {
        this.eventId = eventId;
        this.payload = payload;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
