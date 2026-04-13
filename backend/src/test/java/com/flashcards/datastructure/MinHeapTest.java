package com.flashcards.datastructure;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit Test — verifies MinHeap logic in isolation with no Spring context or mocked dependencies.
class MinHeapTest {

    // Builds a list of cards with the given priorities, assigning sequential IDs starting at 1.
    // IDs are set via reflection since Card entities are not persisted in unit tests.
    private List<Card> cards(int... priorities) throws Exception {
        Deck deck = new Deck("Test", null);
        List<Card> list = new ArrayList<>();
        for (int i = 0; i < priorities.length; i++) {
            Card card = new Card(deck, "front", "back", priorities[i]);
            setId(card, (long) (i + 1));
            list.add(card);
        }
        return list;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }

    @Test
    @DisplayName("buildHeap returns minimum priority first")
    void buildHeapReturnsMinimumFirst() throws Exception {
        MinHeap heap = new MinHeap(cards(3, 1, 2));
        assertEquals(1, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("deleteMin returns nodes in ascending priority order")
    void deleteMinReturnsNodesInAscendingOrder() throws Exception {
        MinHeap heap = new MinHeap(cards(3, 1, 2));

        assertEquals(1, heap.deleteMin().priority);
        assertEquals(2, heap.deleteMin().priority);
        assertEquals(3, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("insert after deleteMin maintains heap order")
    void insertAfterDeleteMaintainsOrder() throws Exception {
        // Heap has [2, 3]. deleteMin gives 2, leaving room to insert.
        // Insert priority 1 — should percolate to top.
        MinHeap heap = new MinHeap(cards(2, 3));

        heap.deleteMin(); // removes priority 2, size drops from 2 to 1
        heap.insert(1, 99L);

        assertEquals(1, heap.deleteMin().priority);
    }

    @Test
    @DisplayName("isEmpty returns true after all elements are removed")
    void isEmptyAfterAllElementsRemoved() throws Exception {
        MinHeap heap = new MinHeap(cards(1, 2));

        assertFalse(heap.isEmpty());
        heap.deleteMin();
        heap.deleteMin();
        assertTrue(heap.isEmpty());
    }
}
