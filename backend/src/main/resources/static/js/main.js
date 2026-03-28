import { fetchDeckById, fetchDeckSummaries } from "./api.js";
import {
    studyModeButtonEl,
    editModeButtonEl,
    backToHomeButtonEl,
    backToDecksButtonEl,
    flashcardEl,
    prevButtonEl,
    nextButtonEl,
    editBackToHomeButtonEl,
    addDeckButtonEl,
    createDeckBackButtonEl,
    createDeckSubmitButtonEl,
    backToEditDecksButtonEl,
    saveDeckButtonEl,
    deleteDeckButtonEl,
    addCardButtonEl,
    saveCardButtonEl,
    deleteCardButtonEl
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
import {
    handleCreateDeck,
    handleDeleteCard,
    handleDeleteDeck,
    handleNewCard,
    handleSaveCard,
    handleSaveDeck,
    initCreateDeckForm,
    loadDeckForEdit,
    loadEditDeckSummaries,
    onDeckSelected
} from "./edit.js";

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

editModeButtonEl.addEventListener("click", async () => {
    showView("editDeckSelection");
    await loadEditDeckSummaries();
});

// — Edit deck selection —

editBackToHomeButtonEl.addEventListener("click", () => {
    showView("welcome");
});

addDeckButtonEl.addEventListener("click", () => {
    showView("createDeck");
    initCreateDeckForm();
});

createDeckBackButtonEl.addEventListener("click", () => {
    showView("editDeckSelection");
});

createDeckSubmitButtonEl.addEventListener("click", async () => {
    await handleCreateDeck();
});

// Register the view transition that fires when a deck is selected from the edit list.
onDeckSelected(() => showView("edit"));

// Wire deck selection — edit.js renders the list, main.js delegates clicks via event delegation.
document.getElementById("edit-deck-list").addEventListener("click", async (e) => {
    const button = e.target.closest("[data-deck-id]");
    if (button) await loadDeckForEdit(Number(button.dataset.deckId));
});

// — Edit view —

backToEditDecksButtonEl.addEventListener("click", async () => {
    showView("editDeckSelection");
    await loadEditDeckSummaries();
});

saveDeckButtonEl.addEventListener("click", async () => {
    await handleSaveDeck();
});

deleteDeckButtonEl.addEventListener("click", async () => {
    await handleDeleteDeck(async () => {
        showView("editDeckSelection");
        await loadEditDeckSummaries();
    });
});

addCardButtonEl.addEventListener("click", () => {
    handleNewCard();
});

saveCardButtonEl.addEventListener("click", async () => {
    await handleSaveCard();
});

deleteCardButtonEl.addEventListener("click", async () => {
    await handleDeleteCard();
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
