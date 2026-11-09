package com.flashcards.datastructure;

import com.flashcards.model.Card;
import java.util.List;

// Holds the two data structures that together drive a study session for one deck.
// The heap determines order — it always surfaces the card with the lowest priority next.
// The map provides data — once the heap returns a card ID, the map retrieves the full card.
// Neither structure knows about the other; the coordination happens here.
public class StudySession {

    private final MinHeap heap;
    private final CardHashMap cardMap;
    private final int deckSize;

    public StudySession(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Cards list must not be null");
        }

        this.heap = new MinHeap(cards);
        this.cardMap = new CardHashMap(cards);
        this.deckSize = cards.size();
    }

    // Returns the next node (cardId + priority) from the heap.
    // The caller is responsible for following up with findCard() and reschedule().
    public MinHeap.HeapNode nextNode() {
        return heap.deleteMin();
    }

    // Looks up full card data by ID using the hash map.
    public Card findCard(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("cardId must not be null");
        }
        return cardMap.search(cardId);
    }

    // Calculates the card's new priority and reinserts it into the heap.
    // Cards marked for further review are placed near the middle of the queue
    // (oldPriority + deckSize / 2) rather than the back (oldPriority + deckSize).
    // Returns the new priority so the service can persist it to the database.
    public int reschedule(Long cardId, int oldPriority, boolean markForReview) {
        if (cardId == null) {
            throw new IllegalArgumentException("cardId must not be null");
        }

        if (oldPriority < 0) {
            throw new IllegalArgumentException("oldPriority must not be negative");
        }

        int newPriority;
        if (markForReview) {
            newPriority = oldPriority + (deckSize / 2);
        } else {
            newPriority = oldPriority + deckSize;
        }
        heap.insert(newPriority, cardId);

        return newPriority;
    }

    public int getDeckSize() {
        return deckSize;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }
}
