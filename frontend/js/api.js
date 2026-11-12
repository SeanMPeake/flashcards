import { getToken, clearSession } from "./auth.js";

// Called when a request comes back 401 (missing or expired token).
// main.js registers a handler that returns the user to the login screen.
let onUnauthorized = () => {};

export function setUnauthorizedHandler(handler) {
    onUnauthorized = handler;
}

// Wraps fetch for every deck and study request: attaches the login token as
// "Authorization: Bearer <token>", and treats a 401 as "logged out".
async function apiFetch(url, options = {}) {
    const headers = { ...(options.headers || {}) };
    const token = getToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
        clearSession();
        onUnauthorized();
        throw new Error("Your session has ended. Please log in again.");
    }
    return response;
}

// Sends signup or login to the user service. On failure, throws an Error whose
// message is the server's and whose `field` says which input it belongs to
// ("username", "password", or null for the form as a whole).
async function sendCredentials(path, username, password) {
    const response = await fetch(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    });

    if (response.ok) {
        return response.json();
    }

    let message = "Something went wrong. Please try again.";
    try {
        message = (await response.json()).message || message;
    } catch {
        // Keep the generic message if the body is not JSON.
    }

    const error = new Error(message);
    if (message.startsWith("username") || response.status === 409) {
        error.field = "username";
    } else if (message.startsWith("password")) {
        error.field = "password";
    } else {
        error.field = null;
    }
    throw error;
}

// Creates an account. Returns { id, username }.
export function signup(username, password) {
    return sendCredentials("/api/users/signup", username, password);
}

// Logs in. Returns { token, tokenType, expiresIn, user: { id, username } }.
export function login(username, password) {
    return sendCredentials("/api/users/login", username, password);
}

// Loads lightweight deck data for the deck selection screen.
export async function fetchDeckSummaries() {
    const response = await apiFetch("/api/decks");
    if (!response.ok) {
        throw new Error("Unable to load decks.");
    }

    return response.json();
}

// Loads the full selected deck, including its cards, for study mode.
export async function fetchDeckById(deckId) {
    const response = await apiFetch(`/api/decks/${deckId}`);
    if (!response.ok) {
        throw new Error("Unable to load selected deck.");
    }

    return response.json();
}

export async function createDeck(data) {
    const response = await apiFetch("/api/decks", {
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
    const response = await apiFetch(`/api/decks/${id}`, {
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
    const response = await apiFetch(`/api/decks/${id}`, { method: "DELETE" });
    if (!response.ok) {
        throw new Error("Unable to delete deck.");
    }
}

export async function createCard(deckId, data) {
    const response = await apiFetch(`/api/decks/${deckId}/cards`, {
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
    const response = await apiFetch(`/api/decks/${deckId}/cards/${cardId}`, {
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
    const response = await apiFetch(`/api/decks/${deckId}/cards/${cardId}`, { method: "DELETE" });
    if (!response.ok) {
        throw new Error("Unable to delete card.");
    }
}

// Builds a fresh study session for the given deck.
// Returns { card, totalCards } — the first card due and the total number of
// cards in the deck, used to initialize the card position display.
export async function startStudySession(deckId) {
    const response = await apiFetch(`/api/study/${deckId}/start`);
    if (!response.ok) {
        throw new Error("Unable to start study session.");
    }
    const data = await response.json();
    return { card: data.card, totalCards: data.totalCards };
}

// Reschedules the card just viewed and returns the next card due.
// markForReview flag determines whether the card is placed in the middle or back of the queue.
export async function nextStudyCard(deckId, cardId, priority, markForReview) {
    const response = await apiFetch(`/api/study/${deckId}/next`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cardId, priority, markForReview })
    });
    if (!response.ok) {
        throw new Error("Unable to load next card.");
    }
    return response.json();
}
