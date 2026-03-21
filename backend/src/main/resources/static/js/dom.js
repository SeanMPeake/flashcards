// Centralized DOM references for the study-mode UI.
// Keeping selectors in one module makes the rest of the frontend easier to read and update.
export const welcomeViewEl = document.getElementById("welcome-view");
export const deckSelectionViewEl = document.getElementById("deck-selection-view");
export const studyViewEl = document.getElementById("study-view");

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
