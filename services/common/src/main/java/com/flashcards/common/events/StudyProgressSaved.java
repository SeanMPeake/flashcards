package com.flashcards.common.events;

import java.time.Instant;

// Published by the deck service each time a batch of study progress is saved, and
// consumed by Review Insights and Analytics (each with its own queue).
//
// This record is the contract between the publisher and its consumers. Changes are
// additive only: fields may be added, never renamed or removed, and consumers read
// only the fields they need (Tolerant Reader), so services can be updated independently.
//
// - eventId: unique per batch ("userId:batchId", built by the deck service from the
//   browser's batch id). A retried save reuses it, so a consumer that has already
//   processed this eventId must skip it (idempotency).
// - sessionId: groups the batches of one study session (one opening of a deck);
//   a session's totals are the sum of its batches.
// - cardsReviewed / cardsFlagged: counts for this batch only.
// - occurredAt: when the batch was saved, in UTC.
public record StudyProgressSaved(
        String eventId,
        long userId,
        long deckId,
        String sessionId,
        int cardsReviewed,
        int cardsFlagged,
        Instant occurredAt) {
}
