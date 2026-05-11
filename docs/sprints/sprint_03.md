# Sprint 3

## Goal

- Implement the three data structures required by course requirements and integrate them into the study experience. Replace the sequential card traversal with a priority-based scheduling algorithm driven by a min-heap and a hash map. Add a navigation history stack and a session coverage tracker.

## User Stories

- As a user, I want the app to determine which card I should study next so that I review cards in an order that prioritizes what I have seen least recently.
- As a user, I want to mark a card for further review so that it reappears sooner than cards I have studied well.
- As a user, I want to see how many unique cards I have reviewed this session so I can track my progress.
- As a user, I want to navigate back to a previously viewed card without affecting the scheduling order.

## Work Completed

- Four custom data structures have been implemented in the com.flashcards.datastructure package. Each is a from-scratch implementation without using Java's built-in collection equivalents for the core logic.

### MinHeap

- A binary min-heap backed by an array. The tree structure maps to array indices: for a node at index i, its left child is at 2i, its right child is at 2i+1, and its parent is at i/2. Index 0 holds a sentinel value of Integer.MIN_VALUE, which causes the percolate-up loop to terminate naturally at the root without a special boundary check.

- The insert operation uses the hole method: rather than swapping elements one at a time, the insertion position is held open and parent values are shifted down until the correct position is found, then the new value is placed. The same hole method is used in percolateDown during deleteMin.

- Initial heap construction uses the O(n) buildHeap method, which percolates down from the last non-leaf node rather than inserting elements one at a time. This is more efficient than repeated insertion for building a heap from an existing list.

- Methods implemented: insert, deleteMin, buildHeap (private), percolateDown (private), isEmpty, size.

### CardHashMap

- A hash table using separate chaining for collision resolution. Each bucket holds a linked list of Node entries. The table size is set to the next prime number greater than or equal to twice the number of cards, which keeps the load factor around 0.5 and reduces average chain length.

- The hash function multiplies the card ID by the prime 31 before taking the modulus, which distributes keys more evenly across buckets and reduces clustering.

- No resize method is implemented. The map is built once at session start and used as a read-only lookup structure. Cards cannot be added or removed during a study session.

- Methods implemented: insert, search, hash (private), nextPrime (private), isPrime (private).

### StudySession

- Holds one instance of MinHeap and CardHashMap for a single deck. The heap determines which card is due next; the map provides the full card data once the heap returns a card ID. Neither structure knows about the other — the coordination happens here.

- The reschedule method calculates the card's new priority after it has been viewed. Normal view: newPriority = oldPriority + deckSize, placing the card at the back of the queue. Mark for further review: newPriority = oldPriority + (deckSize / 2), placing the card approximately in the middle.

- Methods implemented: nextNode, findCard, reschedule, getDeckSize, isEmpty.

### StudySessionManager

- A Spring @Component singleton that holds a Map<Long, StudySession> keyed by deck ID. Uses java.util.HashMap for this map — the custom CardHashMap is used within each session, not for session management itself.

- A fresh session is built from the database each time study mode is entered for a deck. This ensures the heap and map always reflect the latest card priorities. Session invalidation on card mutations is not implemented because the session is always rebuilt on entry to study mode.

### Study Endpoints

- Two endpoints were added under /api/study:
- GET /api/study/{deckId}/start
    Rebuilds the session for the given deck and returns a StudyStartResponse containing the first card due and the total number of cards in the deck. The total card count is included so the frontend can initialize the session coverage display without a separate request.
- POST /api/study/{deckId}/next
    Accepts the card just viewed (cardId, priority, markForReview), reschedules it, persists the updated priority to the database, and returns the next card due as a CardResponse.
- A new request DTO (NextCardRequest) was added for the next endpoint. A new response DTO (StudyStartResponse) wraps the first card and deck size for the start endpoint. An EmptyDeckException was added for the case where a session is started on a deck with no cards.
- The priority field is returned in the card response so the frontend can send it back on the next call. This avoids the need to track priority server-side between requests.

### Frontend Data Structures

- Two custom data structures were implemented in JavaScript to support the study session UI. These are concept demonstrations as the primary data structures under evaluation for this course are the backend MinHeap and CardHashMap.

### NavigationStack

- A cursor-based browsable history stack. Unlike a pure LIFO stack, it supports navigating backward through previously viewed cards and forward again without losing history. The cursor tracks the current position within the items array; the live edge is the most recently pushed item.

- When the user presses Previous, the cursor moves back one position. When the user presses Next and the cursor is not at the live edge, the cursor moves forward through history without calling the backend. Only when the cursor is at the live edge does Next invoke the scheduler.

- Backing storage uses a native JavaScript array. JavaScript arrays resize automatically with amortized O(1) push — the engine handles buffer doubling internally. In a lower-level language, initial capacity would be set to approximately deckSize * 2 to reduce resize frequency.

- Methods implemented: push, back, forward, current, atLiveEdge, atStart, clear.

### CardViewSet

- A hash set with separate chaining that tracks unique card IDs flipped to the answer side during the current session. Used to power the "X of Y cards seen" position display.

- The bucket count is derived from the deck size, rounded up to the nearest power of two. This bounds the load factor at or below 1 at full saturation, and a power-of-two capacity improves key distribution since id % capacity becomes equivalent to a bitmask operation.

- A card is only added to the set when the user flips to the answer side while the cursor is at the live edge. Flips while browsing history are skipped — the card was already counted when it was at the live edge.

- Methods implemented: add, has, count (getter), clear.

### Frontend Wiring

- study.js was updated to replace the placeholder historyStack array and stub functions with the two new data structures. Key changes:
    - NavigationStack and CardViewSet are imported and instantiated when a deck is selected. CardViewSet is sized at construction time using the totalCards value returned by the session start response.
    - showPreviousCard navigates back through history without touching the scheduler. showNextCard checks atLiveEdge — if not at the live edge it calls history.forward() and re-renders; at the live edge it calls the backend as before.
    - flipCard tracks the transition from question to answer side at the live edge and adds the card ID to the CardViewSet. The position display updates immediately when a new card is counted.
    - The Previous button is now enabled and disabled dynamically based on history.atStart() rather than being permanently disabled.

### Test Coverage

- Unit tests were written for MinHeap, CardHashMap, and StudySession covering ordering behavior, collision handling, reschedule placement, and mark-for-review positioning. Service and web layer tests cover the study endpoints including the defensive recovery path and edge cases.

- Tests for StudyControllerTest and StudyServiceImplTest were updated to reflect the new StudyStartResponse return type from startSession, including updated stubs and JSON path assertions.

- All test files include a classification comment distinguishing unit tests, service unit tests, and web layer tests.

## Challenges

- One non-obvious design decision was where to return the card priority in the API response. The priority must travel with the card so the frontend can send it back on the next request for rescheduling. Returning it from the heap node rather than the entity ensures the frontend always has the current in-memory value, which may differ from the database value if a priority update has not yet flushed.

- Another decision was whether to maintain a stateful session (Option B) or rebuild per request (Option A). Option A is simpler but generates excessive database reads during a session. Option B was chosen as the more realistic approach, with session rebuilding on each study mode entry to handle stale state.

- The deck size needed to reach the frontend to initialize the CardViewSet bucket count and populate the session coverage display. Rather than a separate endpoint, totalCards was added to the session start response so both the first card and the deck size arrive in a single round trip.

## Decisions Made

- The hole method was used for both insert (percolate-up) and deleteMin (percolate-down) operations in MinHeap. This reduces the number of actual assignments compared to repeated swaps, and produces cleaner code.

- Separate chaining was chosen over open addressing for CardHashMap. At the expected scale of a single deck (10 to 50 cards), separate chaining is simpler to implement and collision behavior is predictable.

- The in-memory session is rebuilt from the database each time study mode is entered rather than being held indefinitely. This avoids stale data without requiring explicit invalidation logic.

- Card priorities are persisted to the database after each card view so scheduling state carries over between sessions.

- CardViewSet bucket count is rounded to the next power of two above the deck size. This guarantees load factor at or below 1 and improves hash distribution compared to an arbitrary bucket count.

- NavigationStack uses a native JavaScript array rather than a manually resized structure. JavaScript's engine handles dynamic array growth with amortized O(1) push, so implementing explicit doubling in JS would add complexity without meaningful performance benefit at this scale.
