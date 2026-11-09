// Navigation history stack with cursor-based forward/back traversal.
//
// Unlike a pure LIFO stack, this structure supports browsing backward through
// previously viewed cards and forward again without losing history. The cursor
// tracks the current position; the live edge is the most recently pushed item.
// Navigating back and then forward replays history rather than discarding it.
// Only when the cursor is at the live edge and Next is pressed does a new card
// get pushed onto the stack.
//
// Backing storage uses a native JS array. JS arrays resize automatically with
// amortized O(1) push — the engine doubles the internal buffer on overflow, so
// manual pre-allocation is not required here. In a lower-level language (e.g.
// Java), we would initialize capacity to roughly deckSize * 2 to reduce the
// frequency of resize operations.
//
// Note: NavigationStack and CardViewSet are custom implementations for concept
// demonstration. As frontend JS structures supporting session UX, they are not
// the primary data structures under evaluation for this project — the MinHeap
// and CardHashMap on the backend are the intended DS&A deliverables.
export class NavigationStack {
    #items = [];
    #cursor = -1;

    // Pushes a new item at the live edge and advances the cursor.
    // Should only be called when atLiveEdge() is true — navigating forward
    // through existing history uses forward() instead.
    push(item) {
        if (item == null) throw new Error("Cannot push null or undefined onto the navigation stack");
        this.#items.push(item);
        this.#cursor = this.#items.length - 1;
    }

    // Moves the cursor back one position and returns the item there.
    // Does nothing if already at the start.
    back() {
        if (this.#cursor > 0) this.#cursor--;
        return this.#items[this.#cursor];
    }

    // Moves the cursor forward one position and returns the item there.
    // Does nothing if already at the live edge.
    forward() {
        if (this.#cursor < this.#items.length - 1) this.#cursor++;
        return this.#items[this.#cursor];
    }

    // Returns the item at the current cursor position, or null if empty.
    current() {
        return this.#cursor >= 0 ? this.#items[this.#cursor] : null;
    }

    // True when the cursor is at the most recently pushed item.
    // Used to determine whether Next should advance in history or call the backend.
    atLiveEdge() {
        return this.#cursor === this.#items.length - 1;
    }

    // True when the cursor is at the first item — Previous should be disabled.
    atStart() {
        return this.#cursor <= 0;
    }

    // Resets the stack for a new session.
    clear() {
        this.#items = [];
        this.#cursor = -1;
    }
}
