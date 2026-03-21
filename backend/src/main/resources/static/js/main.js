import { fetchDeckById, fetchDeckSummaries } from "./api.js";
import {
    studyModeButtonEl,
    editModeButtonEl,
    backToHomeButtonEl,
    backToDecksButtonEl,
    flashcardEl,
    prevButtonEl,
    nextButtonEl
} from "./dom.js";
import { clearDeckList, renderDeckList, setDeckListStatus, showView } from "./view.js";
import {
    clearSelectedDeck,
    flipCard,
    renderStudyView,
    setSelectedDeck,
    showNextCard,
    showPreviousCard
} from "./study.js";

// The deck selection screen only needs summary data, not full card payloads.
async function loadDeckSummaries() {
    clearDeckList();
    setDeckListStatus("Loading decks...", true);

    try {
        const deckSummaries = await fetchDeckSummaries();
        renderDeckList(deckSummaries, selectDeck);
    } catch (error) {
        setDeckListStatus(error.message, true);
    }
}

// Full deck details are fetched only after a user chooses a deck to study.
async function selectDeck(deckId) {
    try {
        const deck = await fetchDeckById(deckId);
        setSelectedDeck(deck);
        renderStudyView();
        showView("study");
    } catch (error) {
        clearSelectedDeck();
        showView("deckSelection");
        setDeckListStatus(error.message, true);
    }
}

// Wire up navigation and study interactions after the module loads.
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
