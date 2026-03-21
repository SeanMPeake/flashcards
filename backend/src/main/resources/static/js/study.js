import {
    deckTitleEl,
    cardPositionEl,
    flashcardEl,
    cardFaceLabelEl,
    cardTextEl,
    studyEmptyStateEl,
    studyEmptyStateHeadingEl,
    studyEmptyStateTextEl,
    prevButtonEl,
    nextButtonEl
} from "./dom.js";

// State for the active study session.
// This tracks the selected deck, the current card position, and whether the card is flipped.
let selectedDeck = null;
let currentCardIndex = 0;
let isFlipped = false;

function hasSelectedDeck() {
    return selectedDeck !== null;
}

function hasCards(deck) {
    return Boolean(deck && deck.cards && deck.cards.length > 0);
}

// When a new deck is selected, always restart at the first card and show the front side.
function resetStudyPosition() {
    currentCardIndex = 0;
    isFlipped = false;
}

function setStudyEmptyState(title, message) {
    studyEmptyStateHeadingEl.textContent = title;
    studyEmptyStateTextEl.textContent = message;
    studyEmptyStateEl.classList.remove("hidden");
    flashcardEl.classList.add("hidden");
}

export function setSelectedDeck(deck) {
    selectedDeck = deck;
    resetStudyPosition();
}

export function clearSelectedDeck() {
    selectedDeck = null;
}

// Renders either the empty study state or the currently selected card.
export function renderStudyView() {
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

// Card flipping is handled entirely in the frontend by swapping which side's text is shown.
export function flipCard() {
    if (!hasCards(selectedDeck)) {
        return;
    }

    isFlipped = !isFlipped;
    renderStudyView();
}

export function showPreviousCard() {
    if (!hasSelectedDeck() || currentCardIndex === 0) {
        return;
    }

    currentCardIndex -= 1;
    isFlipped = false;
    renderStudyView();
}

export function showNextCard() {
    if (!hasCards(selectedDeck) || currentCardIndex >= selectedDeck.cards.length - 1) {
        return;
    }

    currentCardIndex += 1;
    isFlipped = false;
    renderStudyView();
}
