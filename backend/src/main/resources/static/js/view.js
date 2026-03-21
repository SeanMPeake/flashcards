import {
    welcomeViewEl,
    deckSelectionViewEl,
    studyViewEl,
    deckListEl,
    deckListStatusEl
} from "./dom.js";

// Only one main screen should be visible at a time.
export function showView(viewName) {
    welcomeViewEl.classList.add("hidden");
    deckSelectionViewEl.classList.add("hidden");
    studyViewEl.classList.add("hidden");

    if (viewName === "welcome") {
        welcomeViewEl.classList.remove("hidden");
    } else if (viewName === "deckSelection") {
        deckSelectionViewEl.classList.remove("hidden");
    } else if (viewName === "study") {
        studyViewEl.classList.remove("hidden");
    }
}

export function setDeckListStatus(message, isVisible) {
    deckListStatusEl.textContent = message;
    if (isVisible) {
        deckListStatusEl.classList.remove("hidden");
    } else {
        deckListStatusEl.classList.add("hidden");
    }
}

// Builds one deck-selection button from summary data returned by the list endpoint.
function createDeckButton(deck, onSelect) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "deck-button";

    const name = document.createElement("span");
    name.className = "deck-name";
    name.textContent = deck.name;

    const description = document.createElement("span");
    description.className = "deck-description";
    description.textContent = deck.description || "No description provided.";

    const meta = document.createElement("span");
    meta.className = "deck-meta";
    meta.textContent = `${deck.cardCount} cards`;

    button.append(name, description, meta);
    button.addEventListener("click", () => {
        onSelect(deck.id);
    });

    return button;
}

export function renderDeckList(deckSummaries, onSelect) {
    deckListEl.replaceChildren();

    if (deckSummaries.length === 0) {
        setDeckListStatus("No decks available.", true);
        return;
    }

    setDeckListStatus("", false);

    deckSummaries.forEach((deck) => {
        deckListEl.appendChild(createDeckButton(deck, onSelect));
    });
}

export function clearDeckList() {
    deckListEl.replaceChildren();
}
