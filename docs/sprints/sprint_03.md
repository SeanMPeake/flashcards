User Stories

    As a user, I want the app to determine which card I should study next so that I review cards in an order that prioritizes what I have seen least recently.
    As a user, I want to mark a card for further review so that it reappears sooner than cards I have studied well.
    As a user, I want to see how many unique cards I have reviewed this session so I can track my progress.
    As a user, I want to navigate back to a previously viewed card without affecting the scheduling order.

Sprint 3

Goal

    Implement the three data structures required by course requirements and integrate them into the study experience. Replace the sequential card traversal with a priority-based scheduling algorithm driven by a min-heap and a hash map. Add a navigation history stack and a session coverage tracker.

Work Completed (In Progress)

    Four custom data structures have been implemented in the com.flashcards.datastructure package. Each is a from-scratch implementation without using Java's built-in collection equivalents for the core logic.

MinHeap

    A binary min-heap backed by an array. The tree structure maps to array indices: for a node at index i, its left child is at 2i, its right child is at 2i+1, and its parent is at i/2. Index 0 holds a sentinel value of Integer.MIN_VALUE, which causes the percolate-up loop to terminate naturally at the root without a special boundary check.

    The insert operation uses the hole method: rather than swapping elements one at a time, the insertion position is held open and parent values are shifted down until the correct position is found, then the new value is placed. The same hole method is used in percolateDown during deleteMin.

    Initial heap construction uses the O(n) buildHeap method, which percolates down from the last non-leaf node rather than inserting elements one at a time. This is more efficient than repeated insertion for building a heap from an existing list.

    Methods implemented: insert, deleteMin, buildHeap (private), percolateDown (private), isEmpty, size.

CardHashMap

    A hash table using separate chaining for collision resolution. Each bucket holds a linked list of Node entries. The table size is set to the next prime number greater than or equal to twice the number of cards, which keeps the load factor around 0.5 and reduces average chain length.

    The hash function multiplies the card ID by the prime 31 before taking the modulus, which distributes keys more evenly across buckets and reduces clustering.

    No resize method is implemented. The map is built once at session start and used as a read-only lookup structure. Cards cannot be added or removed during a study session.

    Methods implemented: insert, search, hash (private), nextPrime (private), isPrime (private).

StudySession

    Holds one instance of MinHeap and CardHashMap for a single deck. The heap determines which card is due next; the map provides the full card data once the heap returns a card ID. Neither structure knows about the other — the coordination happens here.

    The reschedule method calculates the card's new priority after it has been viewed. Normal view: newPriority = oldPriority + deckSize, placing the card at the back of the queue. Mark for further review: newPriority = oldPriority + (deckSize / 2), placing the card approximately in the middle.

    Methods implemented: nextNode, findCard, reschedule, isEmpty.

StudySessionManager

    A Spring @Component singleton that holds a Map<Long, StudySession> keyed by deck ID. Uses java.util.HashMap for this map — the custom CardHashMap is used within each session, not for session management itself.

    A fresh session is built from the database each time study mode is entered for a deck. This ensures the heap and map always reflect the latest card priorities. Session invalidation on card mutations is not implemented because the session is always rebuilt on entry to study mode.

Study Endpoints

    Two new endpoints were added under /api/study:

    GET /api/study/{deckId}/start
    Rebuilds the session for the given deck and returns the first card due.

    POST /api/study/{deckId}/next
    Accepts the card just viewed (cardId, priority, markForReview), reschedules it, persists the updated priority to the database, and returns the next card due.

    A new request DTO (NextCardRequest) was added for the next endpoint. An EmptyDeckException was added for the case where a session is started on a deck with no cards.

    The priority field is returned in the card response so the frontend can send it back on the next call. This avoids the need to track priority server-side between requests.

Frontend Wiring (In Progress)

    The study mode frontend is being updated to replace the sequential card traversal with calls to the new scheduling endpoints. The mark for review checkbox is being added to the card face.

Test Coverage

    Unit tests were written for MinHeap, CardHashMap, and StudySession covering ordering behavior, collision handling, reschedule placement, and mark-for-review positioning. Service and web layer tests cover the study endpoints including the defensive recovery path and edge cases.

    All test files include a classification comment distinguishing unit tests, service unit tests, and web layer tests.

Challenges

    One non-obvious design decision was where to return the card priority in the API response. The priority must travel with the card so the frontend can send it back on the next request for rescheduling. Returning it from the heap node rather than the entity ensures the frontend always has the current in-memory value, which may differ from the database value if a priority update has not yet flushed.

    Another decision was whether to maintain a stateful session (Option B) or rebuild per request (Option A). Option A is simpler but generates excessive database reads during a session. Option B was chosen as the more realistic approach, with session rebuilding on each study mode entry to handle stale state.

Decisions Made

    The hole method was used for both insert (percolate-up) and deleteMin (percolate-down) operations in MinHeap. This reduces the number of actual assignments compared to repeated swaps, and produces cleaner code.

    Separate chaining was chosen over open addressing for CardHashMap. At the expected scale of a single deck (10 to 50 cards), separate chaining is simpler to implement and collision behavior is predictable.

    The in-memory session is rebuilt from the database each time study mode is entered rather than being held indefinitely. This avoids stale data without requiring explicit invalidation logic.

    Card priorities are persisted to the database after each card view so scheduling state carries over between sessions.

Next Steps

    Remaining sprint 3 work includes:

    A frontend navigation stack to support reviewing previously seen cards without affecting the scheduler. Going back moves through the history stack; pressing next from the live edge resumes scheduling.

    A frontend HashSet to track unique card IDs viewed this session, replacing the current sequential card counter with an accurate session coverage display.

    Frontend integration of the mark for review checkbox with the scheduling logic.
