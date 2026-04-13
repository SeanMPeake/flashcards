package com.flashcards.datastructure;

import com.flashcards.model.Card;
import java.util.List;

public class CardHashMap {

    // One node in a bucket's chain. Collisions are handled by separate chaining —
    // each bucket holds a linked list of nodes that hashed to the same index.
    private static class Node {
        Long cardId;
        Card card;
        Node next;

        Node(Long cardId, Card card) {
            this.cardId = cardId;
            this.card = card;
            this.next = null;
        }
    }

    private Node[] buckets;
    private int tableSize;

    // Table size is set to the next prime >= cards.size() * 2 to keep the
    // load factor around 0.5, which reduces the average chain length.
    public CardHashMap(List<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("Cards list must not be null");
        tableSize = nextPrime(cards.size() * 2);
        buckets = new Node[tableSize];

        for (Card card : cards) {
            insert(card.getId(), card);
        }
    }

    // Multiplying by the prime 31 before modding distributes keys more evenly
    // across buckets. Prime multipliers reduce the chance that similar input
    // values collide at the same index — a composite multiplier would cause
    // certain key patterns to cluster. 31 is the same constant used by
    // Java's own String.hashCode().
    private int hash(Long cardId) {
        return (int)(Math.abs(cardId * 31L) % tableSize);
    }

    // Inserts a card at the head of the chain at its hashed bucket.
    public void insert(Long cardId, Card card) {
        if (cardId == null) throw new IllegalArgumentException("cardId must not be null");
        if (card == null) throw new IllegalArgumentException("card must not be null");
        int index = hash(cardId);
        Node newNode = new Node(cardId, card);
        newNode.next = buckets[index];
        buckets[index] = newNode;
    }

    // Returns the card for the given ID, or null if not found.
    // Walks the chain at the hashed bucket to find the matching entry.
    public Card search(Long cardId) {
        if (cardId == null) throw new IllegalArgumentException("cardId must not be null");
        int index = hash(cardId);
        Node current = buckets[index];
        while (current != null) {
            if (current.cardId.equals(cardId)) {
                return current.card;
            }
            current = current.next;
        }
        return null;
    }

    // No resize method is implemented. This map is built once at session start
    // and used as a read-only lookup structure during study mode. Cards cannot
    // be added or removed while a study session is active.

    private int nextPrime(int n) {
        if (n <= 2) return 2;
        int candidate = n % 2 == 0 ? n + 1 : n;
        while (!isPrime(candidate)) {
            candidate += 2;
        }
        return candidate;
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
