const welcomeViewEl = document.getElementById("welcome-view");
const deckSelectionViewEl = document.getElementById("deck-selection-view");
const studyViewEl = document.getElementById("study-view");

const studyModeButtonEl = document.getElementById("study-mode-button");
const editModeButtonEl = document.getElementById("edit-mode-button");
const backToHomeButtonEl = document.getElementById("back-to-home-button");
const backToDecksButtonEl = document.getElementById("back-to-decks-button");

const deckListEl = document.getElementById("deck-list");
const deckListStatusEl = document.getElementById("deck-list-status");

const deckTitleEl = document.getElementById("deck-title");
const cardPositionEl = document.getElementById("card-position");
const flashcardEl = document.getElementById("flashcard");
const cardFaceLabelEl = document.getElementById("card-face-label");
const cardTextEl = document.getElementById("card-text");
const studyEmptyStateEl = document.getElementById("study-empty-state");
const studyEmptyStateHeadingEl = studyEmptyStateEl.querySelector("h3");
const studyEmptyStateTextEl = studyEmptyStateEl.querySelector("p");
const prevButtonEl = document.getElementById("prev-button");
const nextButtonEl = document.getElementById("next-button");

let deckSummaries = [];
let selectedDeck = null;
let currentCardIndex = 0;
let isFlipped = false;

function hasSelectedDeck() {
    return selectedDeck !== null;
}

function hasCards(deck) {
    return Boolean(deck && deck.cards && deck.cards.length > 0);
}

function resetStudyPosition() {
    currentCardIndex = 0;
    isFlipped = false;
}

function showView(viewName) {
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

function setDeckListStatus(message, isVisible) {
    deckListStatusEl.textContent = message;
    if (isVisible) {
        deckListStatusEl.classList.remove("hidden");
    } else {
        deckListStatusEl.classList.add("hidden");
    }
}

function createDeckButton(deck) {
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
        selectDeck(deck.id);
    });

    return button;
}

function renderDeckList() {
    deckListEl.replaceChildren();

    if (deckSummaries.length === 0) {
        setDeckListStatus("No decks available.", true);
        return;
    }

    setDeckListStatus("", false);

    deckSummaries.forEach((deck) => {
        deckListEl.appendChild(createDeckButton(deck));
    });
}

async function loadDeckSummaries() {
    deckListEl.replaceChildren();
    setDeckListStatus("Loading decks...", true);

    try {
        const response = await fetch("/api/decks");
        if (!response.ok) {
            throw new Error("Unable to load decks.");
        }

        deckSummaries = await response.json();
        renderDeckList();
    } catch (error) {
        deckSummaries = [];
        setDeckListStatus(error.message, true);
    }
}

async function selectDeck(deckId) {
    try {
        const response = await fetch(`/api/decks/${deckId}`);
        if (!response.ok) {
            throw new Error("Unable to load selected deck.");
        }

        selectedDeck = await response.json();
        resetStudyPosition();
        renderStudyView();
        showView("study");
    } catch (error) {
        selectedDeck = null;
        showView("deckSelection");
        setDeckListStatus(error.message, true);
    }
}

function setStudyEmptyState(title, message) {
    studyEmptyStateHeadingEl.textContent = title;
    studyEmptyStateTextEl.textContent = message;
    studyEmptyStateEl.classList.remove("hidden");
    flashcardEl.classList.add("hidden");
}

function renderStudyView() {
    if (!hasSelectedDeck()) {
        deckTitleEl.textContent = "Deck Title";
        cardPositionEl.textContent = "";
        setStudyEmptyState("No Deck Selected", "Choose a deck to begin studying.");
        prevButtonEl.disabled = true;
        nextButtonEl.disabled = true;
        return;
    }

    deckTitleEl.textContent = selectedDeck.name;

    if (!hasCards(selectedDeck)) {
        cardPositionEl.textContent = "No cards";
        setStudyEmptyState("No Cards Found", "This deck does not have any cards yet.");
        prevButtonEl.disabled = true;
        nextButtonEl.disabled = true;
        return;
    }

    const currentCard = selectedDeck.cards[currentCardIndex];

    studyEmptyStateEl.classList.add("hidden");
    flashcardEl.classList.remove("hidden");

    cardPositionEl.textContent = `Card ${currentCardIndex + 1} of ${selectedDeck.cards.length}`;
    cardFaceLabelEl.textContent = isFlipped ? "Answer" : "Question";
    cardTextEl.textContent = isFlipped ? currentCard.backText : currentCard.frontText;

    prevButtonEl.disabled = currentCardIndex === 0;
    nextButtonEl.disabled = currentCardIndex === selectedDeck.cards.length - 1;
}

function flipCard() {
    if (!hasCards(selectedDeck)) {
        return;
    }

    isFlipped = !isFlipped;
    renderStudyView();
}

function showPreviousCard() {
    if (!hasSelectedDeck() || currentCardIndex === 0) {
        return;
    }

    currentCardIndex -= 1;
    isFlipped = false;
    renderStudyView();
}

function showNextCard() {
    if (!hasCards(selectedDeck) || currentCardIndex >= selectedDeck.cards.length - 1) {
        return;
    }

    currentCardIndex += 1;
    isFlipped = false;
    renderStudyView();
}

studyModeButtonEl.addEventListener("click", async () => {
    showView("deckSelection");
    await loadDeckSummaries();
});

editModeButtonEl.addEventListener("click", () => {
    showView("welcome");
});

backToHomeButtonEl.addEventListener("click", () => {
    showView("welcome");
});

backToDecksButtonEl.addEventListener("click", () => {
    showView("deckSelection");
});

flashcardEl.addEventListener("click", flipCard);
prevButtonEl.addEventListener("click", showPreviousCard);
nextButtonEl.addEventListener("click", showNextCard);

renderStudyView();
showView("welcome");
