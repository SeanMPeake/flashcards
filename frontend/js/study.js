import { fetchDeckById } from "./api.js";
import { NavigationStack } from "./NavigationStack.js";
import { CardViewSet } from "./CardViewSet.js";
import { finishSaving, recordPriority, startSaving } from "./studySaver.js";
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
    markForReviewCheckboxEl,
    saveStatusEl
} from "./dom.js";

// Study runs entirely in the browser. Opening a deck loads all its cards once;
// after that, choosing the next card needs no server call. New priorities are
// saved in the background by studySaver.js.
//
// Scheduling: the card with the lowest priority comes next. After a card is
// reviewed, its priority grows by the deck size (back of the queue), or by half
// the deck size when marked for review (middle of the queue).

// State for the active study session.
let currentDeckId = null;
let cards = [];           // every card in the deck: { id, frontText, backText, priority }
let currentCard = null;
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
    cardPositionEl.textContent = `${seen} of ${cards.length} cards seen`;
}

// The card due next: the lowest priority, with ties going to the lower card id so
// the order is always predictable. A deck is a few dozen cards, so a simple scan
// is instant and easier to read than a heap.
function nextDueCard() {
    let due = cards[0];
    for (const card of cards) {
        if (card.priority < due.priority || (card.priority === due.priority && card.id < due.id)) {
            due = card;
        }
    }
    return due;
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
// Loads the whole deck once and shows the card due first.
export async function setSelectedDeck(deckId, deckName) {
    await finishSaving();   // save anything left from a previously studied deck

    currentDeckId = deckId;
    cards = [];
    currentCard = null;
    isFlipped = false;
    history.clear();

    deckTitleEl.textContent = deckName;
    saveStatusEl.textContent = "";

    try {
        const deck = await fetchDeckById(deckId);
        cards = deck.cards;

        if (cards.length === 0) {
            cardPositionEl.textContent = "";
            setStudyEmptyState("No Cards Found", "This deck does not have any cards yet.");
            prevButtonEl.disabled = true;
            nextButtonEl.disabled = true;
            return;
        }

        startSaving(deckId);
        viewedCards = new CardViewSet(cards.length);
        currentCard = nextDueCard();
        history.push(currentCard);
        renderCard();
    } catch (error) {
        setStudyEmptyState("Unable to Start Session", error.message);
        prevButtonEl.disabled = true;
        nextButtonEl.disabled = true;
    }
}

// Called when leaving study (Back, Home, Log out): saves outstanding progress,
// then clears the session.
export async function leaveStudy() {
    await finishSaving();
    clearSelectedDeck();
}

export function clearSelectedDeck() {
    currentDeckId = null;
    cards = [];
    currentCard = null;
    history.clear();
    if (viewedCards) viewedCards.clear();
}

// Renders the initial state before a deck is selected.
export function renderStudyView() {
    if (!currentCard) {
        deckTitleEl.textContent = "Deck Title";
        cardPositionEl.textContent = "";
        saveStatusEl.textContent = "";
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
// If the cursor is not at the live edge, moves forward through history. At the
// live edge, reschedules the current card and shows the card due next, with no
// server call; the new priority is saved in the background.
export function showNextCard() {
    if (!currentCard) {
        return;
    }

    if (!history.atLiveEdge()) {
        currentCard = history.forward();
        isFlipped = false;
        renderCard();
        return;
    }

    const step = markForReviewCheckboxEl.checked ? Math.floor(cards.length / 2) : cards.length;
    currentCard.priority += step;
    recordPriority(currentCard.id, currentCard.priority);

    currentCard = nextDueCard();
    isFlipped = false;
    history.push(currentCard);
    renderCard();
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
