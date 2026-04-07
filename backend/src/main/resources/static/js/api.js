// Loads lightweight deck data for the deck selection screen.
export async function fetchDeckSummaries() {
    const response = await fetch("/api/decks");
    if (!response.ok) {
        throw new Error("Unable to load decks.");
    }

    return response.json();
}

// Loads the full selected deck, including its cards, for study mode.
export async function fetchDeckById(deckId) {
    const response = await fetch(`/api/decks/${deckId}`);
    if (!response.ok) {
        throw new Error("Unable to load selected deck.");
    }

    return response.json();
}

export async function createDeck(data) {
    const response = await fetch("/api/decks", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error("Unable to create deck.");
    }
    return response.json();
}

export async function updateDeck(id, data) {
    const response = await fetch(`/api/decks/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error("Unable to save deck.");
    }
    return response.json();
}

export async function deleteDeck(id) {
    const response = await fetch(`/api/decks/${id}`, { method: "DELETE" });
    if (!response.ok) {
        throw new Error("Unable to delete deck.");
    }
}

export async function createCard(deckId, data) {
    const response = await fetch(`/api/decks/${deckId}/cards`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error("Unable to create card.");
    }
    return response.json();
}

export async function updateCard(deckId, cardId, data) {
    const response = await fetch(`/api/decks/${deckId}/cards/${cardId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error("Unable to save card.");
    }
    return response.json();
}

export async function deleteCard(deckId, cardId) {
    const response = await fetch(`/api/decks/${deckId}/cards/${cardId}`, { method: "DELETE" });
    if (!response.ok) {
        throw new Error("Unable to delete card.");
    }
}

// Builds a fresh study session for the given deck.
// Returns { card, totalCards } — the first card due and the total number of
// cards in the deck, used to initialize the card position display.
export async function startStudySession(deckId) {
    const response = await fetch(`/api/study/${deckId}/start`);
    if (!response.ok) {
        throw new Error("Unable to start study session.");
    }
    const data = await response.json();
    return { card: data.card, totalCards: data.totalCards };
}

// Reschedules the card just viewed and returns the next card due.
// markForReview flag determines whether the card is placed in the middle or back of the queue.
export async function nextStudyCard(deckId, cardId, priority, markForReview) {
    const response = await fetch(`/api/study/${deckId}/next`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cardId, priority, markForReview })
    });
    if (!response.ok) {
        throw new Error("Unable to load next card.");
    }
    return response.json();
}
