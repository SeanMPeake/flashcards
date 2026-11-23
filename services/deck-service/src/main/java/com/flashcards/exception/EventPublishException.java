package com.flashcards.exception;

// The study progress was saved, but its event couldn't be sent to the message broker.
// Reported as 503 so the browser retries the same batch: the save is repeated
// harmlessly (priorities are never lowered) and the event is sent again with the same
// eventId, which consumers skip if they already have it.
public class EventPublishException extends RuntimeException {

    public EventPublishException(Throwable cause) {
        super("Study progress could not be recorded right now. It will be retried.", cause);
    }
}
