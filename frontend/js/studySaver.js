import { savePriorities } from "./api.js";
import { saveStatusEl } from "./dom.js";

// Saves study progress (new card priorities) to the deck service in the background.
//
// Studying never waits for the server: new priorities are collected here and sent
// in a batch every few cards and when the user leaves study. If a save fails
// because the server or database can't be reached, studying carries on and the
// save is retried with exponential backoff. This keeps study available during an
// outage (the AP choice) at the cost of progress that isn't saved yet: anything
// still unsaved is lost if the tab is closed while the server is unreachable.

const SAVE_EVERY = 5;           // cards reviewed between saves
const FIRST_RETRY_MS = 1000;    // then 2s, 4s, 8s... up to the maximum
const MAX_RETRY_MS = 30000;

let deckId = null;
let pending = new Map();        // cardId -> newest priority not yet saved
let reviewsSinceSave = 0;
let retryDelay = FIRST_RETRY_MS;
let retryTimer = null;
let inFlight = null;            // the save currently being sent, if any

function showStatus(text) {
    saveStatusEl.textContent = text;
}

// Starts collecting progress for a newly opened deck.
export function startSaving(newDeckId) {
    deckId = newDeckId;
    pending = new Map();
    reviewsSinceSave = 0;
    retryDelay = FIRST_RETRY_MS;
    showStatus("All progress saved");
}

// Records a card's new priority; saves once enough cards have been reviewed.
export function recordPriority(cardId, priority) {
    pending.set(cardId, priority);
    reviewsSinceSave++;
    if (reviewsSinceSave >= SAVE_EVERY) {
        saveNow();
    } else if (!retryTimer) {
        showStatus("Progress not yet saved");
    }
}

// Sends everything not yet saved. Returns a promise that settles when this attempt
// finishes (successfully or not); failures schedule their own retry.
export function saveNow({ keepalive = false } = {}) {
    if (inFlight) {
        return inFlight;            // a save is already on its way
    }
    if (deckId === null || pending.size === 0) {
        return Promise.resolve();
    }

    clearTimeout(retryTimer);
    retryTimer = null;

    const batchDeckId = deckId;
    const batch = [...pending].map(([cardId, priority]) => ({ cardId, priority }));
    reviewsSinceSave = 0;
    showStatus("Saving…");

    inFlight = savePriorities(batchDeckId, batch, keepalive)
        .then(() => {
            // Remove what was sent, unless the card has moved on again since.
            for (const { cardId, priority } of batch) {
                if (pending.get(cardId) === priority) {
                    pending.delete(cardId);
                }
            }
            retryDelay = FIRST_RETRY_MS;
            showStatus(pending.size === 0 ? "All progress saved" : "Progress not yet saved");
        })
        .catch((error) => {
            if (error.permanent) {
                // The server rejected this data or the deck is gone: retrying can't help.
                pending.clear();
                showStatus("Some progress couldn't be saved");
                return;
            }
            // Server or connection trouble: try again later, waiting longer each time.
            // A little randomness (jitter) keeps many clients from retrying in lockstep.
            const wait = retryDelay + Math.random() * retryDelay * 0.3;
            retryTimer = setTimeout(() => saveNow(), wait);
            retryDelay = Math.min(retryDelay * 2, MAX_RETRY_MS);
            showStatus("Can't reach the server; will keep trying");
        })
        .finally(() => {
            inFlight = null;
        });

    return inFlight;
}

// Called when leaving study (Back, Home, Log out, opening another deck): makes one
// last save attempt and stops retrying for this deck.
export async function finishSaving() {
    await saveNow();
    // If a save was already in flight, priorities recorded meanwhile are still
    // pending: send them too.
    if (pending.size > 0) {
        await saveNow();
    }
    clearTimeout(retryTimer);
    retryTimer = null;
    deckId = null;
}

// If the tab is closed or hidden, send what's pending; keepalive lets the request
// complete even as the page goes away.
window.addEventListener("pagehide", () => {
    saveNow({ keepalive: true });
});
