package com.flashcards.datastructure;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit Test — verifies MinHeap logic in isolation with no Spring context or mocked dependencies.
class MinHeapTest {

    // Builds a list of cards with the given priorities.
    // Card IDs are null (unsaved entities) since heap ordering only depends on priority.
    private List<Card> cards(int... priorities) {
        Deck deck = new Deck("Test", null);
        List<Card> list = new ArrayList<>();
        for (int p : priorities) {
            list.add(new Card(deck, "front", "back", p));
        }
        return list;
    }

    @Test
    @DisplayName("buildHeap returns minimum priority first")
    void buildHeapReturnsMinimumFirst() {
        MinHeap heap = new MinHeap(cards(3, 1, 2));
        assertEquals(1, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("deleteMin returns nodes in ascending priority order")
    void deleteMinReturnsNodesInAscendingOrder() {
        MinHeap heap = new MinHeap(cards(3, 1, 2));

        assertEquals(1, heap.deleteMin().priority);
        assertEquals(2, heap.deleteMin().priority);
        assertEquals(3, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("insert after deleteMin maintains heap order")
    void insertAfterDeleteMaintainsOrder() {
        // Heap has [2, 3]. deleteMin gives 2, leaving room to insert.
        // Insert priority 1 — should percolate to top.
        MinHeap heap = new MinHeap(cards(2, 3));

        heap.deleteMin(); // removes priority 2, size drops from 2 to 1
        heap.insert(1, null);

        assertEquals(1, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("isEmpty returns true after all elements are removed")
    void isEmptyAfterAllElementsRemoved() {
        MinHeap heap = new MinHeap(cards(1, 2));

        assertFalse(heap.isEmpty());
        heap.deleteMin();
        heap.deleteMin();
        assertTrue(heap.isEmpty());
    }
}
