package com.flashcards.datastructure;

import com.flashcards.model.Card;
import java.util.Arrays;
import java.util.List;

public class MinHeap {

    // Represents one card's position in the scheduling queue.
    // Returned by deleteMin so the caller has both the card ID and its priority.
    public static class HeapNode {
        public final int priority;
        public final Long cardId;

        public HeapNode(int priority, Long cardId) {
            this.priority = priority;
            this.cardId = cardId;
        }
    }

    private HeapNode[] array;
    private int size;

    // Builds the heap from a list of cards using the O(n) buildHeap method.
    public MinHeap(List<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("Cards list must not be null");
        size = cards.size();
        array = new HeapNode[size + 1];
        array[0] = new HeapNode(Integer.MIN_VALUE, null); // sentinel — stops percolate-up naturally at index 0

        for (int i = 0; i < cards.size(); i++) {
            array[i + 1] = new HeapNode(cards.get(i).getPriority(), cards.get(i).getId());
        }

        buildHeap();
    }

    // Establishes the heap property in O(n) time by percolating down from
    // the last non-leaf node. Leaf nodes (past size/2) are already valid.
    // This is Floyd's algorithm — more efficient than inserting one element
    // at a time, which would cost O(n log n) due to repeated percolate-up calls.
    private void buildHeap() {
        for (int i = size / 2; i >= 1; i--) {
            percolateDown(i);
        }
    }

    // Adds a card to the heap. Uses the hole method: shift parents down
    // without swapping until the correct position is found, then insert.
    public void insert(int priority, Long cardId) {
        if (cardId == null) throw new IllegalArgumentException("cardId must not be null");
        if (size >= array.length - 1) {
            array = Arrays.copyOf(array, array.length * 2);
        }
        size++;
        int hole = size;
        while (array[hole / 2].priority > priority) {
            array[hole] = array[hole / 2];
            hole = hole / 2;
        }
        array[hole] = new HeapNode(priority, cardId);
    }

    // Removes and returns the node with the lowest priority (next card due).
    public HeapNode deleteMin() {
        if (isEmpty()) throw new IllegalStateException("Heap is empty");
        HeapNode min = array[1];
        array[1] = array[size];
        size--;
        percolateDown(1);
        return min;
    }

    // Restores the heap property downward from the given position.
    // Uses the hole method: saves the displaced node, shifts smaller
    // children up until the correct position is found, then places it.
    private void percolateDown(int hole) {
        HeapNode tmp = array[hole];
        int child;
        for (; hole * 2 <= size; hole = child) {
            child = hole * 2;
            // If right child exists and is smaller, prefer it.
            // child == size means left child is the last node — no right child exists.
            if (child != size && array[child + 1].priority < array[child].priority) {
                child++;
            }
            if (array[child].priority < tmp.priority) {
                array[hole] = array[child];
            } else {
                break;
            }
        }
        array[hole] = tmp;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
