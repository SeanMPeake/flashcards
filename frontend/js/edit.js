import {
    createCard,
    createDeck,
    deleteCard,
    deleteDeck,
    fetchDeckById,
    fetchDeckSummaries,
    updateCard,
    updateDeck
} from "./api.js";
import {
    cardEditorEmptyStateEl,
    cardEditorFormEl,
    cardSidebarListEl,
    createDeckDescriptionInputEl,
    createDeckNameInputEl,
    createDeckStatusEl,
    deleteCardButtonEl,
    editCardBackEl,
    editCardFrontEl,
    editDeckDescriptionInputEl,
    editDeckListEl,
    editDeckListStatusEl,
    editDeckNameInputEl,
    editStatusEl
} from "./dom.js";

// State for the active edit session.
let selectedDeck = null;
let selectedCard = null;
let isNewCard = false;

// — Deck selection screen —

export async function loadEditDeckSummaries() {
    editDeckListEl.replaceChildren();
    setEditDeckListStatus("Loading decks...", true);

    try {
        const summaries = await fetchDeckSummaries();
        renderEditDeckList(summaries);
    } catch (error) {
        setEditDeckListStatus(error.message, true);
    }
}

function renderEditDeckList(summaries) {
    editDeckListEl.replaceChildren();

    if (summaries.length === 0) {
        setEditDeckListStatus("No decks yet. Create one above.", true);
        return;
    }

    setEditDeckListStatus("", false);

    summaries.forEach((deck) => {
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

        button.dataset.deckId = deck.id;
        button.append(name, description, meta);
        editDeckListEl.appendChild(button);
    });
}

function setEditDeckListStatus(message, isVisible) {
    editDeckListStatusEl.textContent = message;
    if (isVisible) {
        editDeckListStatusEl.classList.remove("hidden");
    } else {
        editDeckListStatusEl.classList.add("hidden");
    }
}

export function initCreateDeckForm() {
    createDeckNameInputEl.value = "";
    createDeckDescriptionInputEl.value = "";
    createDeckStatusEl.classList.add("hidden");
    createDeckNameInputEl.focus();
}

export async function handleCreateDeck() {
    const name = createDeckNameInputEl.value.trim();
    if (!name) {
        createDeckNameInputEl.focus();
        return;
    }

    try {
        const description = createDeckDescriptionInputEl.value.trim();
        const created = await createDeck({ name, description });
        // After creation, go directly into the edit view for the new deck.
        await loadDeckForEdit(created.id);
    } catch (error) {
        createDeckStatusEl.textContent = error.message;
        createDeckStatusEl.classList.remove("hidden");
    }
}

// — Edit view —

let _onDeckSelectedCallback = null;

// Registers a callback so main.js can perform the view switch when a deck is selected.
// Importing showView directly in edit.js would create a circular dependency through dom.js.
export function onDeckSelected(callback) {
    _onDeckSelectedCallback = callback;
}

export async function loadDeckForEdit(deckId) {
    try {
        const deck = await fetchDeckById(deckId);
        selectedDeck = deck;
        selectedCard = null;
        isNewCard = false;
        renderEditView();
        if (_onDeckSelectedCallback) _onDeckSelectedCallback();
    } catch (error) {
        setEditDeckListStatus(error.message, true);
    }
}

function renderEditView() {
    editDeckNameInputEl.value = selectedDeck.name;
    editDeckDescriptionInputEl.value = selectedDeck.description || "";
    setEditStatus("", false);
    renderCardSidebar();
    showCardEditorEmptyState();
}

function renderCardSidebar() {
    cardSidebarListEl.replaceChildren();

    if (!selectedDeck.cards || selectedDeck.cards.length === 0) {
        return;
    }

    selectedDeck.cards.forEach((card) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "card-sidebar-item";
        button.textContent = card.frontText;
        button.dataset.cardId = card.id;

        if (selectedCard && selectedCard.id === card.id) {
            button.classList.add("active");
        }

        button.addEventListener("click", () => selectCard(card));
        cardSidebarListEl.appendChild(button);
    });
}

function selectCard(card) {
    selectedCard = card;
    isNewCard = false;

    // Update active state in sidebar.
    cardSidebarListEl.querySelectorAll(".card-sidebar-item").forEach((btn) => {
        btn.classList.toggle("active", Number(btn.dataset.cardId) === card.id);
    });

    editCardFrontEl.value = card.frontText;
    editCardBackEl.value = card.backText;
    showCardEditorForm();
}

function showCardEditorForm() {
    cardEditorEmptyStateEl.classList.add("hidden");
    cardEditorFormEl.classList.remove("hidden");
    deleteCardButtonEl.classList.remove("hidden");
}

function showCardEditorEmptyState() {
    cardEditorFormEl.classList.add("hidden");
    cardEditorEmptyStateEl.classList.remove("hidden");
}

export function handleNewCard() {
    selectedCard = null;
    isNewCard = true;

    cardSidebarListEl.querySelectorAll(".card-sidebar-item").forEach((btn) => {
        btn.classList.remove("active");
    });

    editCardFrontEl.value = "";
    editCardBackEl.value = "";
    deleteCardButtonEl.classList.add("hidden");
    showCardEditorForm();
    editCardFrontEl.focus();
}

export async function handleSaveDeck() {
    const name = editDeckNameInputEl.value.trim();
    if (!name) {
        editDeckNameInputEl.focus();
        return;
    }

    try {
        const description = editDeckDescriptionInputEl.value.trim();
        const updated = await updateDeck(selectedDeck.id, { name, description });
        selectedDeck = { ...selectedDeck, name: updated.name, description: updated.description };
        setEditStatus("Deck saved.", true);
    } catch (error) {
        setEditStatus(error.message, true);
    }
}

export async function handleDeleteDeck(onDeleted) {
    if (!confirm(`Delete "${selectedDeck.name}"? This will also delete all its cards.`)) return;

    try {
        await deleteDeck(selectedDeck.id);
        selectedDeck = null;
        selectedCard = null;
        isNewCard = false;
        onDeleted();
    } catch (error) {
        setEditStatus(error.message, true);
    }
}

export async function handleSaveCard() {
    const frontText = editCardFrontEl.value.trim();
    const backText = editCardBackEl.value.trim();

    if (!frontText || !backText) {
        if (!frontText) editCardFrontEl.focus();
        return;
    }

    try {
        if (isNewCard) {
            const created = await createCard(selectedDeck.id, { frontText, backText });
            selectedDeck.cards.push(created);
            selectedCard = created;
            isNewCard = false;
            deleteCardButtonEl.classList.remove("hidden");
        } else {
            const updated = await updateCard(selectedDeck.id, selectedCard.id, { frontText, backText });
            const idx = selectedDeck.cards.findIndex((c) => c.id === selectedCard.id);
            if (idx !== -1) selectedDeck.cards[idx] = updated;
            selectedCard = updated;
        }
        renderCardSidebar();
        setEditStatus("Card saved.", true);
    } catch (error) {
        setEditStatus(error.message, true);
    }
}

export async function handleDeleteCard() {
    if (!confirm("Delete this card?")) return;

    try {
        await deleteCard(selectedDeck.id, selectedCard.id);
        selectedDeck.cards = selectedDeck.cards.filter((c) => c.id !== selectedCard.id);
        selectedCard = null;
        isNewCard = false;
        renderCardSidebar();
        showCardEditorEmptyState();
        setEditStatus("Card deleted.", true);
    } catch (error) {
        setEditStatus(error.message, true);
    }
}

function setEditStatus(message, isVisible) {
    editStatusEl.textContent = message;
    if (isVisible) {
        editStatusEl.classList.remove("hidden");
    } else {
        editStatusEl.classList.add("hidden");
    }
}

