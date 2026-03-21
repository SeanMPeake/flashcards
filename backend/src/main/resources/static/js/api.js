export async function fetchDeckSummaries() {
    const response = await fetch("/api/decks");
    if (!response.ok) {
        throw new Error("Unable to load decks.");
    }

    return response.json();
}

export async function fetchDeckById(deckId) {
    const response = await fetch(`/api/decks/${deckId}`);
    if (!response.ok) {
        throw new Error("Unable to load selected deck.");
    }

    return response.json();
}
