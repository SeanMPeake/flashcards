import { startStudySession, nextStudyCard } from "./api.js";
import { NavigationStack } from "./NavigationStack.js";
import { CardViewSet } from "./CardViewSet.js";
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
let currentDeckId = null;
let currentCard = null;   // { id, frontText, backText, priority }
let currentDeckCardCount = 0;
let isFlipped = false;

// Browsable navigation history — tracks cards viewed this session so the user
// can step backward and forward without affecting the scheduler.
const history = new NavigationStack();

// Tracks unique cards flipped to the answer side this session.
// Initialized when a deck is selected so the bucket count can be sized to the deck.
let viewedCards = null;

function setStudyEmptyState(title, message) {
    studyEmptyStateHeadingEl.textContent = title;
    studyEmptyStateTextEl.textContent = message;
    studyEmptyStateEl.classList.remove("hidden");
    flashcardEl.classList.add("hidden");
}

// Updates the card position indicator with the current unique-view count.
function updatePositionDisplay() {
    const seen = viewedCards ? viewedCards.count : 0;
    cardPositionEl.textContent = `${seen} of ${currentDeckCardCount} cards seen`;
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

    prevButtonEl.disabled = history.atStart();
    nextButtonEl.disabled = false;

    updatePositionDisplay();
}

// Called when the user selects a deck from the deck selection screen.
// Starts a fresh session on the backend and renders the first card.
export async function setSelectedDeck(deckId, deckName) {
    currentDeckId = deckId;
    currentCard = null;
    currentDeckCardCount = 0;
    isFlipped = false;
    history.clear();

    deckTitleEl.textContent = deckName;

    try {
        const { card, totalCards } = await startStudySession(deckId);
        currentCard = card;
        currentDeckCardCount = totalCards;
        viewedCards = new CardViewSet(totalCards);
        history.push(currentCard);
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
    currentDeckCardCount = 0;
    history.clear();
    if (viewedCards) viewedCards.clear();
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
// When flipping to the answer side at the live edge of history, records the card
// as viewed in the session set. Each card is only counted once regardless of
// how many times it is flipped.
export function flipCard() {
    if (!currentCard) {
        return;
    }

    const wasFlipped = isFlipped;
    isFlipped = !isFlipped;

    cardFaceLabelEl.textContent = isFlipped ? "Answer" : "Question";
    cardTextEl.textContent = isFlipped ? currentCard.backText : currentCard.frontText;

    // Only count a card as viewed when flipping to the answer side for the first
    // time. Flips while browsing history are skipped — the card was already
    // counted (or will be counted) when it was at the live edge.
    if (!wasFlipped && isFlipped && history.atLiveEdge()) {
        viewedCards.add(currentCard.id);
        updatePositionDisplay();
    }
}

// Navigates to the next card.
// If the cursor is not at the live edge, moves forward through history without
// calling the backend. At the live edge, asks the scheduler for the next card.
export async function showNextCard() {
    if (!currentCard) {
        return;
    }

    if (!history.atLiveEdge()) {
        currentCard = history.forward();
        isFlipped = false;
        renderCard();
        return;
    }

    const markForReview = markForReviewCheckboxEl.checked;

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
        history.push(currentCard);
        renderCard();
    } catch (error) {
        nextButtonEl.disabled = false;
        cardPositionEl.textContent = error.message;
        setTimeout(() => updatePositionDisplay(), 5000);
    }
}

// Navigates back to the previous card in history without affecting the scheduler.
export function showPreviousCard() {
    if (!currentCard || history.atStart()) {
        return;
    }
    currentCard = history.back();
    isFlipped = false;
    renderCard();
}
