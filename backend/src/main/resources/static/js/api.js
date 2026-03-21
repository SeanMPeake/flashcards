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
