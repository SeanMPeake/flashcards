import { savePriorities } from "./api.js";
import { saveStatusEl } from "./dom.js";

// Saves study progress (new card priorities) to the deck service in the background.
//
// Studying never waits for the server: new priorities are collected here and sent
// in batches every few cards and when the user leaves study. If a save fails
// because the server or database can't be reached, studying carries on and the
// save is retried with exponential backoff, even after the user leaves study or
// opens another deck. This keeps study available during an outage (the AP choice);
// the cost is that progress still unsaved when the tab is closed, or when the user
// logs out, during an outage is lost.

const SAVE_EVERY = 5;           // cards reviewed between saves
const FIRST_RETRY_MS = 1000;    // then 2s, 4s, 8s... up to the maximum
const MAX_RETRY_MS = 30000;

// Unsaved priorities, kept per deck so that progress from a deck the user has
// left keeps being retried: deckId -> Map(cardId -> newest unsaved priority).
const pendingByDeck = new Map();

let reviewsSinceSave = 0;
let retryDelay = FIRST_RETRY_MS;
let retryTimer = null;
let inFlight = null;            // the save currently being sent, if any
let lastRejected = false;       // the server permanently rejected some progress

function hasPending() {
    for (const cards of pendingByDeck.values()) {
        if (cards.size > 0) {
            return true;
        }
    }
    return false;
}

// Shows the overall save state on the study screen.
export function showSaveStatus() {
    let text = "All progress saved";
    if (inFlight) {
        text = "Saving…";
    } else if (retryTimer) {
        text = "Can't reach the server; will keep trying";
    } else if (hasPending()) {
        text = "Progress not yet saved";
    } else if (lastRejected) {
        text = "Some progress couldn't be saved";
    }
    saveStatusEl.textContent = text;
}

// Records a card's new priority; saves once enough cards have been reviewed.
export function recordPriority(deckId, cardId, priority) {
    if (!pendingByDeck.has(deckId)) {
        pendingByDeck.set(deckId, new Map());
    }
    pendingByDeck.get(deckId).set(cardId, priority);
    reviewsSinceSave++;

    // During an outage a retry is already scheduled and will carry these entries
    // too; sending early would bypass the backoff.
    if (reviewsSinceSave >= SAVE_EVERY && !retryTimer) {
        saveNow();
    } else {
        showSaveStatus();
    }
}

// Removes what a batch sent, unless a card has moved on again since.
function removeSent(deckId, batch) {
    const cards = pendingByDeck.get(deckId);
    if (!cards) {
        return;
    }
    for (const { cardId, priority } of batch) {
        if (cards.get(cardId) === priority) {
            cards.delete(cardId);
        }
    }
    if (cards.size === 0) {
        pendingByDeck.delete(deckId);
    }
}

function scheduleRetry() {
    // Wait longer after each failure; a little randomness (jitter) keeps many
    // clients from retrying in lockstep against a recovering server.
    const wait = retryDelay + Math.random() * retryDelay * 0.3;
    retryTimer = setTimeout(() => {
        retryTimer = null;
        saveNow();
    }, wait);
    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_MS);
}

// Sends each deck's unsaved progress in turn. Stops at the first failure that
// might succeed later and schedules a retry for everything still pending.
async function sendAll() {
    for (const [deckId, cards] of [...pendingByDeck]) {
        const batch = [...cards].map(([cardId, priority]) => ({ cardId, priority }));
        try {
            await savePriorities(deckId, batch);
            removeSent(deckId, batch);
            retryDelay = FIRST_RETRY_MS;
            lastRejected = false;
        } catch (error) {
            if (error.unauthorized) {
                // The login has ended: there is no token to retry with.
                stopSaving();
                return;
            }
            if (error.permanent) {
                // Rejected data or a deleted deck: drop only this batch's entries.
                removeSent(deckId, batch);
                lastRejected = true;
                continue;
            }
            scheduleRetry();
            return;
        }
    }
}

// Sends everything not yet saved. Returns a promise that settles when this attempt
// finishes; a failure schedules its own retry.
export function saveNow() {
    if (inFlight) {
        return inFlight;            // a save is already on its way
    }
    if (!hasPending()) {
        return Promise.resolve();
    }

    clearTimeout(retryTimer);
    retryTimer = null;
    reviewsSinceSave = 0;

    inFlight = sendAll().finally(() => {
        inFlight = null;
        showSaveStatus();
    });
    showSaveStatus();
    return inFlight;
}

// Stops all saving and forgets unsaved progress (used once the login has ended).
export function stopSaving() {
    clearTimeout(retryTimer);
    retryTimer = null;
    pendingByDeck.clear();
    reviewsSinceSave = 0;
    retryDelay = FIRST_RETRY_MS;
}

// If the tab is closed or hidden, send everything pending straight away, even if
// another save is already on its way (re-sending a value is harmless: the server
// keeps the highest). Every save uses keepalive, so these requests can complete
// as the page goes away.
window.addEventListener("pagehide", () => {
    for (const [deckId, cards] of pendingByDeck) {
        const batch = [...cards].map(([cardId, priority]) => ({ cardId, priority }));
        savePriorities(deckId, batch).catch(() => {});
    }
});
