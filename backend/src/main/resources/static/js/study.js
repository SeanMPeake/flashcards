import { startStudySession, nextStudyCard } from "./api.js";
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
    nextButtonEl,
    markForReviewCheckboxEl
} from "./dom.js";

// State for the active study session.
// currentDeckId and currentCard replace the old deck object and index-based traversal.
// The scheduler on the backend determines card order; the frontend only tracks what's currently showing.
let currentDeckId = null;
let currentCard = null;   // { id, frontText, backText, priority }
let isFlipped = false;

// Navigation history stack — stores previously viewed cards so the user can go back
// without affecting the scheduler. Implemented as a plain JS array used as a stack.
// TODO: replace with custom Stack data structure in a later sprint step.
const historyStack = [];

function setStudyEmptyState(title, message) {
    studyEmptyStateHeadingEl.textContent = title;
    studyEmptyStateTextEl.textContent = message;
    studyEmptyStateEl.classList.remove("hidden");
    flashcardEl.classList.add("hidden");
}

function renderCard() {
    if (!currentCard) {
        return;
    }

    studyEmptyStateEl.classList.add("hidden");
    flashcardEl.classList.remove("hidden");

    cardFaceLabelEl.textContent = isFlipped ? "Answer" : "Question";
    cardTextEl.textContent = isFlipped ? currentCard.backText : currentCard.frontText;

    // Reset the mark for review checkbox each time a new card is shown.
    markForReviewCheckboxEl.checked = false;

    // Previous is disabled until the navigation stack is fully wired up.
    // The button remains in the DOM so it can be enabled when the stack is implemented.
    prevButtonEl.disabled = true;
    nextButtonEl.disabled = false;

    // Card position display is a placeholder — will show unique cards viewed
    // once the session HashSet tracker is implemented.
    cardPositionEl.textContent = "Studying...";
}

// Called when the user selects a deck from the deck selection screen.
// Starts a fresh session on the backend and renders the first card.
export async function setSelectedDeck(deckId, deckName) {
    currentDeckId = deckId;
    currentCard = null;
    isFlipped = false;
    historyStack.length = 0;

    deckTitleEl.textContent = deckName;

    try {
        currentCard = await startStudySession(deckId);
        renderCard();
    } catch (error) {
        setStudyEmptyState("Unable to Start Session", error.message);
        prevButtonEl.disabled = true;
        nextButtonEl.disabled = true;
    }
}

export function clearSelectedDeck() {
    currentDeckId = null;
    currentCard = null;
    historyStack.length = 0;
}

// Renders the initial state before a deck is selected.
export function renderStudyView() {
    if (!currentCard) {
        deckTitleEl.textContent = "Deck Title";
        cardPositionEl.textContent = "";
        setStudyEmptyState("No Deck Selected", "Choose a deck to begin studying.");
        prevButtonEl.disabled = true;
        nextButtonEl.disabled = true;
    }
}

// Flips between the question and answer side of the current card.
export function flipCard() {
    if (!currentCard) {
        return;
    }
    isFlipped = !isFlipped;
    cardFaceLabelEl.textContent = isFlipped ? "Answer" : "Question";
    cardTextEl.textContent = isFlipped ? currentCard.backText : currentCard.frontText;
}

// Advances to the next card. Pushes the current card onto the history stack,
// reads the mark for review flag, then asks the backend for the next scheduled card.
export async function showNextCard() {
    if (!currentCard) {
        return;
    }

    const markForReview = markForReviewCheckboxEl.checked;

    // Push current card onto history before moving forward.
    historyStack.push(currentCard);

    nextButtonEl.disabled = true;

    try {
        const nextCard = await nextStudyCard(
            currentDeckId,
            currentCard.id,
            currentCard.priority,
            markForReview
        );
        currentCard = nextCard;
        isFlipped = false;
        renderCard();
    } catch (error) {
        // If the next card call fails, restore the card we were on.
        currentCard = historyStack.pop();
        nextButtonEl.disabled = false;
        cardPositionEl.textContent = error.message;
    }
}

// Navigates back through the history stack without touching the scheduler.
// Disabled until the stack is fully implemented — placeholder for future sprint work.
export function showPreviousCard() {
    // TODO: implement with custom Stack data structure
}
