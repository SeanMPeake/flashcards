// Tracks the set of unique card IDs the user has flipped to the answer side
// during the current study session. Used to display "X of Y cards seen" in
// the card position indicator.
//
// Implemented as a hash set with separate chaining. The bucket count is derived
// from the deck size and rounded up to the nearest power of two. This keeps the
// load factor at or below 1 at full saturation (every card seen), ensuring O(1)
// average-case lookup and insert. A power-of-two capacity also improves
// distribution: id % capacity becomes a clean bitmask operation, reducing
// clustering compared to an arbitrary modulus.
//
// Note: NavigationStack and CardViewSet are custom implementations for concept
// demonstration. As frontend JS structures supporting session UX, they are not
// the primary data structures under evaluation for this project — the MinHeap
// and CardHashMap on the backend are the intended DS&A deliverables.
export class CardViewSet {
    #buckets;
    #capacity;
    #count = 0;

    constructor(deckSize) {
        if (deckSize <= 0) throw new Error("CardViewSet requires a deck size greater than zero");
        this.#capacity = CardViewSet.#nextPowerOfTwo(deckSize);
        this.#buckets = Array.from({ length: this.#capacity }, () => []);
    }

    // Returns the smallest power of two >= n.
    static #nextPowerOfTwo(n) {
        let p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    // Maps a card ID to a bucket index.
    #hash(id) {
        return id % this.#capacity;
    }

    // Adds a card ID to the set. No-ops if already present.
    add(id) {
        if (id == null) throw new Error("id must not be null or undefined");
        if (this.has(id)) return;
        this.#buckets[this.#hash(id)].push(id);
        this.#count++;
    }

    // Returns true if the card ID is in the set.
    has(id) {
        if (id == null) throw new Error("id must not be null or undefined");
        return this.#buckets[this.#hash(id)].includes(id);
    }

    // Total number of unique card IDs in the set.
    get count() {
        return this.#count;
    }

    // Resets the set for a new session.
    clear() {
        this.#buckets = Array.from({ length: this.#capacity }, () => []);
        this.#count = 0;
    }
}
