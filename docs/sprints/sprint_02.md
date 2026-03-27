User Stories

    As a user, I want to enter study mode so I can start a flashcard study session.
    As a user, I want to choose a deck so I can study a specific set of flashcards.
    As a user, I want to flip a flashcard so I can view both the question and answer.
    As a user, I want to navigate forward and backward through cards so I can review the whole deck.

Sprint 2

Goal

    Build the first end-to-end study workflow by introducing a service layer, separating API response models, and creating a basic frontend for selecting and reviewing decks.

Work Completed

    The second sprint focused on building a vertical slice of the study experience. The existing deck endpoints were refactored so that controllers no longer accessed repositories directly. A service layer was introduced for deck read operations, and DTOs were added to separate API responses from JPA entities.

    The deck API was divided into two response types. The list endpoint now returns deck summaries for selection, while the single-deck endpoint returns full deck details including associated cards. The summary query was later refined so card counts are calculated directly in the repository query rather than by loading full card collections.

    A basic frontend was added within the Spring Boot application using static HTML, CSS, and JavaScript. This frontend now supports a study mode entry screen, a deck selection screen, and a study screen for reviewing flashcards.

The current study workflow supports:

    Entering study mode
    Viewing available decks
    Selecting a deck
    Traversing cards with previous and next controls
    Flipping a card by clicking on it
    Displaying the current card position within the deck

    Test coverage was also expanded during this sprint. Controller tests were updated to reflect the summary/detail API split, service-layer unit tests were added for DTO mapping and missing-deck behavior, and a DTO-level test was added to support the summary query constructor behavior.

Challenges

    A key challenge during the sprint was deciding how much frontend structure to introduce without overcomplicating the project too early. The frontend was kept inside the Spring Boot project so the full vertical slice could be implemented with minimal setup.

    Another challenge was shaping the API to support both deck selection and card study cleanly. This led to separating summary responses from full deck detail responses and later refining the summary path to better match that design.

Decisions Made

    The frontend was implemented as static resources within the backend project rather than as a separate frontend application.

    Deck responses were split into summary and detail DTOs so the deck selection screen would not depend on full card payloads. The summary endpoint was later refined to calculate card counts directly in the repository query.

    The study workflow was intentionally kept sequential for this sprint. More advanced scheduling behavior using the planned priority queue was deferred until the basic review flow was working.

Next Steps

    The next sprint will focus on expanding functionality for deck and card creation or editing, improving test coverage where needed, and continuing development of the study experience.

    Future work will also include implementing scheduling logic for review order using the planned data structure approach.
