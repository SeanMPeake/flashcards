// Centralized DOM references for all views.
// Keeping selectors in one module makes the rest of the frontend easier to read and update.
export const welcomeViewEl = document.getElementById("welcome-view");
export const deckSelectionViewEl = document.getElementById("deck-selection-view");
export const studyViewEl = document.getElementById("study-view");
export const editDeckSelectionViewEl = document.getElementById("edit-deck-selection-view");
export const createDeckViewEl = document.getElementById("create-deck-view");
export const editViewEl = document.getElementById("edit-view");

export const studyModeButtonEl = document.getElementById("study-mode-button");
export const editModeButtonEl = document.getElementById("edit-mode-button");
export const backToHomeButtonEl = document.getElementById("back-to-home-button");
export const backToDecksButtonEl = document.getElementById("back-to-decks-button");

export const deckListEl = document.getElementById("deck-list");
export const deckListStatusEl = document.getElementById("deck-list-status");

export const deckTitleEl = document.getElementById("deck-title");
export const cardPositionEl = document.getElementById("card-position");
export const flashcardEl = document.getElementById("flashcard");
export const cardFaceLabelEl = document.getElementById("card-face-label");
export const cardTextEl = document.getElementById("card-text");
export const studyEmptyStateEl = document.getElementById("study-empty-state");
export const studyEmptyStateHeadingEl = studyEmptyStateEl.querySelector("h3");
export const studyEmptyStateTextEl = studyEmptyStateEl.querySelector("p");
export const prevButtonEl = document.getElementById("prev-button");
export const nextButtonEl = document.getElementById("next-button");
export const markForReviewCheckboxEl = document.getElementById("mark-for-review-checkbox");

// Edit deck selection
export const editBackToHomeButtonEl = document.getElementById("edit-back-to-home-button");
export const addDeckButtonEl = document.getElementById("add-deck-button");
export const editDeckListEl = document.getElementById("edit-deck-list");
export const editDeckListStatusEl = document.getElementById("edit-deck-list-status");

// Create deck
export const createDeckBackButtonEl = document.getElementById("create-deck-back-button");
export const createDeckNameInputEl = document.getElementById("create-deck-name-input");
export const createDeckDescriptionInputEl = document.getElementById("create-deck-description-input");
export const createDeckSubmitButtonEl = document.getElementById("create-deck-submit-button");
export const createDeckStatusEl = document.getElementById("create-deck-status");

// Edit view
export const editDeckNameInputEl = document.getElementById("edit-deck-name-input");
export const editDeckDescriptionInputEl = document.getElementById("edit-deck-description-input");
export const saveDeckButtonEl = document.getElementById("save-deck-button");
export const deleteDeckButtonEl = document.getElementById("delete-deck-button");
export const backToEditDecksButtonEl = document.getElementById("back-to-edit-decks-button");
export const editStatusEl = document.getElementById("edit-status");
export const addCardButtonEl = document.getElementById("add-card-button");
export const cardSidebarListEl = document.getElementById("card-sidebar-list");
export const cardEditorEmptyStateEl = document.getElementById("card-editor-empty-state");
export const cardEditorFormEl = document.getElementById("card-editor-form");
export const editCardFrontEl = document.getElementById("edit-card-front");
export const editCardBackEl = document.getElementById("edit-card-back");
export const saveCardButtonEl = document.getElementById("save-card-button");
export const deleteCardButtonEl = document.getElementById("delete-card-button");
