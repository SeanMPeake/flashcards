# Sprint 2

## Goal

- Build the first end-to-end study workflow by introducing a service layer, separating API response models, and creating a basic frontend for selecting and reviewing decks. Then extend the system with full CRUD support for decks and cards, including a working edit mode in the frontend.

## User Stories

- As a user, I want to enter study mode so I can start a flashcard study session.
- As a user, I want to choose a deck so I can study a specific set of flashcards.
- As a user, I want to flip a flashcard so I can view both the question and answer.
- As a user, I want to navigate forward and backward through cards so I can review the whole deck.
- As a user, I want to enter edit mode so I can manage my decks and cards.
- As a user, I want to create a new deck so I can organize a new set of flashcards.
- As a user, I want to edit a deck's name and description so I can keep it up to date.
- As a user, I want to delete a deck so I can remove content I no longer need.
- As a user, I want to add a card to a deck so I can build out my study material.
- As a user, I want to edit a card's front and back text so I can correct or improve it.
- As a user, I want to delete a card so I can remove content I no longer need.

## Work Completed

- The second sprint focused on building a complete vertical slice of the application, covering both the study experience and a full edit mode for managing content.

- The existing deck endpoints were refactored so that controllers no longer accessed repositories directly. A service layer was introduced for deck read operations, and DTOs were added to separate API responses from JPA entities. The deck API was divided into two response types: the list endpoint returns deck summaries for selection, while the single-deck endpoint returns full deck details including associated cards. Card counts in the summary response are calculated directly in a repository query rather than by loading full card collections.

- A global exception handler was added using @RestControllerAdvice to centralize error responses across all controllers. A shared ErrorResponse DTO is returned for all error cases, including 404s for missing decks and cards, 400s for validation failures, and a generic 500 for unhandled exceptions. @PrePersist and @PreUpdate lifecycle hooks were added to the entity models to automate timestamp management.

- Full CRUD support was implemented for both decks and cards. New request DTOs with Bean Validation were introduced for create and update operations. A CardController was added with endpoints nested under /api/decks/{deckId}/cards. The card service enforces deck ownership, treating a card that belongs to a different deck as not found. All write operations use @Transactional and read operations use @Transactional(readOnly = true).

- A basic frontend was added within the Spring Boot application using static HTML, CSS, and JavaScript. The frontend is organized into ES6 modules: api.js handles all fetch calls, dom.js centralizes element references, view.js manages view transitions, study.js handles study mode state and rendering, edit.js handles edit mode state and rendering, and main.js wires up all event listeners.

### The current study workflow supports:

- Entering study mode
- Viewing available decks
- Selecting a deck
- Traversing cards with previous and next controls
- Flipping a card by clicking on it
- Displaying the current card position within the deck

### The edit mode supports:

- Entering edit mode from the home screen
- Viewing all decks in an editable list
- Creating a new deck through a dedicated create screen
- Editing a deck's name and description
- Deleting a deck
- Selecting a card from a scrollable sidebar
- Adding a new card to a deck
- Editing a card's front and back text
- Deleting a card

- Test coverage was expanded throughout the sprint. Controller tests cover all CRUD endpoints including validation and error cases. Service-layer unit tests cover all operations including not-found scenarios and card ownership enforcement.

## Challenges

- A key challenge was deciding how much frontend structure to introduce without overcomplicating the project. The frontend was kept inside the Spring Boot project so the full vertical slice could be implemented with minimal setup. As the edit mode grew, managing view transitions and shared state across modules required careful attention to avoid circular dependencies between modules.

- Implementing the card sidebar required a CSS fix that was non-obvious: flex children default to min-height: auto, which prevents them from shrinking below their content size. Setting min-height: 0 on the scrollable container was required for overflow-y: auto to work correctly inside a flex column layout.

## Decisions Made

- The frontend was implemented as static resources within the backend project rather than as a separate frontend application.

- Deck responses were split into summary and detail DTOs so the deck selection screen would not depend on full card payloads.

- A centralized error handler was preferred over per-controller error handling to keep error response formatting consistent. RFC 7807 Problem Details format was considered but rejected in favor of a simpler two-field ErrorResponse (status, message) appropriate for a single-consumer demo project.

- Card endpoints were nested under /api/decks/{deckId}/cards rather than using a flat /api/cards path to reflect the ownership relationship and make the API more self-descriptive.

- Individual GET endpoints for single cards were intentionally omitted. The full deck detail endpoint already returns all cards, so fetching a card in isolation would serve no purpose in the current application.

- Card ownership is enforced in the service layer. If a card exists but belongs to a different deck than the one in the request path, it is treated as not found. This keeps the API contract consistent from the client's perspective.

- View transitions in the frontend are coordinated through main.js. The edit module does not import the view module directly to avoid a circular dependency. Instead, edit.js exposes an onDeckSelected(callback) registration function that main.js uses to trigger the view switch when a deck is loaded for editing.

## Next Steps

- Sprint 3 will focus on implementing the three data structures required by course requirements:

    - A min-heap priority queue for card scheduling, which will determine review order based on card priority.

    - A HashMap for card lookup by ID to support efficient access during study sessions.

    - A stack for navigation history in study mode, replacing the current linear index-based traversal.

- If time permits during sprint 3, UI polish work may also be addressed, including a carousel-style deck selector and frontend testing.
