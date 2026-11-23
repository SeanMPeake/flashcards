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
//
// Batches: reviews collect in an open batch per deck. When a save starts, each open
// batch is sealed: it gets a batchId and never changes again, so a retry sends
// exactly the same batch with the same id. The deck service uses the batchId as the
// StudyProgressSaved event's eventId, which lets consumers skip a batch they have
// already counted. Reviews made while a batch is being retried go into a new batch.

const SAVE_EVERY = 5;           // cards reviewed between saves
const FIRST_RETRY_MS = 1000;    // then 2s, 4s, 8s... up to the maximum
const MAX_RETRY_MS = 30000;

// Progress still being collected, one open batch per deck:
// deckId -> { sessionId, cardsReviewed, cardsFlagged, priorities: Map(cardId -> newest priority) }
const openByDeck = new Map();

// Sealed batches waiting to be sent, oldest first:
// { deckId, batchId, sessionId, cardsReviewed, cardsFlagged, priorities: [{ cardId, priority }] }
const sealed = [];

let reviewsSinceSave = 0;
let retryDelay = FIRST_RETRY_MS;
let retryTimer = null;
let inFlight = null;            // the save currently being sent, if any
let lastRejected = false;       // the server permanently rejected some progress

function hasPending() {
    return sealed.length > 0 || openByDeck.size > 0;
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

// Closes a deck's open batch and queues it for sending with a new batchId.
function seal(deckId) {
    const open = openByDeck.get(deckId);
    if (!open) {
        return;
    }
    openByDeck.delete(deckId);
    sealed.push({
        deckId,
        batchId: crypto.randomUUID(),
        sessionId: open.sessionId,
        cardsReviewed: open.cardsReviewed,
        cardsFlagged: open.cardsFlagged,
        priorities: [...open.priorities].map(([cardId, priority]) => ({ cardId, priority }))
    });
}

function sealAll() {
    for (const deckId of [...openByDeck.keys()]) {
        seal(deckId);
    }
}

// Records a reviewed card's new priority, and whether it was marked for review;
// saves once enough cards have been reviewed.
export function recordPriority(deckId, sessionId, cardId, priority, flagged) {
    // A new session on this deck starts a new batch, so a batch never mixes sessions.
    if (openByDeck.has(deckId) && openByDeck.get(deckId).sessionId !== sessionId) {
        seal(deckId);
    }
    if (!openByDeck.has(deckId)) {
        openByDeck.set(deckId, { sessionId, cardsReviewed: 0, cardsFlagged: 0, priorities: new Map() });
    }

    const open = openByDeck.get(deckId);
    open.priorities.set(cardId, priority);
    open.cardsReviewed++;
    if (flagged) {
        open.cardsFlagged++;
    }
    reviewsSinceSave++;

    // During an outage a retry is already scheduled and will carry these entries
    // too; sending early would bypass the backoff.
    if (reviewsSinceSave >= SAVE_EVERY && !retryTimer) {
        saveNow();
    } else {
        showSaveStatus();
    }
}

function removeBatch(batch) {
    const index = sealed.indexOf(batch);
    if (index !== -1) {
        sealed.splice(index, 1);
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

// Sends the sealed batches in order. Stops at the first failure that might succeed
// later and schedules a retry; the failed batch stays queued, unchanged.
async function sendAll() {
    for (const batch of [...sealed]) {
        try {
            await savePriorities(batch.deckId, batch);
            removeBatch(batch);
            retryDelay = FIRST_RETRY_MS;
            lastRejected = false;
        } catch (error) {
            if (error.unauthorized) {
                // The login has ended: there is no token to retry with.
                stopSaving();
                return;
            }
            if (error.permanent) {
                // Rejected data or a deleted deck: drop only this batch.
                removeBatch(batch);
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
    sealAll();

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
    openByDeck.clear();
    sealed.length = 0;
    reviewsSinceSave = 0;
    retryDelay = FIRST_RETRY_MS;
}

// If the tab is closed or hidden, send everything pending straight away, even if
// another save is already on its way. Sending a batch twice is harmless: it keeps
// its batchId, the server keeps the highest priority, and consumers skip the
// duplicate event. Every save uses keepalive, so these requests can complete as
// the page goes away.
window.addEventListener("pagehide", () => {
    sealAll();
    for (const batch of sealed) {
        savePriorities(batch.deckId, batch).catch(() => {});
    }
});
