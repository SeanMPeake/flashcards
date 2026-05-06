Flashcard Study Application
Spring 2026 Software Development Project

1. Project Overview

Flashcards is a web-based study application that allows users to organize flashcards into decks and review them through a browser-based interface backed by a REST API. Users can create and manage decks and cards through an edit mode, and study card content in a scheduled order determined by a priority-based algorithm. The project was developed iteratively across four two-week sprints using an agile approach.

2. Problem Statement

Students and self-learners often need a simple way to organize study material and review it repeatedly. Many flashcard tools focus primarily on storage rather than structured review. This project supports more intentional study behavior by combining deck management with algorithm-driven scheduling to determine which cards should be reviewed next, placing less-recently reviewed cards at the back of the queue and allowing users to flag cards for earlier re-review.

3. Architecture Overview

Backend
Spring Boot REST API providing endpoints for deck management, card management, and study session control. Business logic is handled in a service layer, keeping controllers thin. All database access goes through Spring Data JPA repository interfaces.

Database
PostgreSQL stores deck and card data. The schema is managed through Hibernate DDL auto-update. A startup DataLoader seeds three decks with twelve cards each for demonstration purposes.

Frontend
Static HTML, CSS, and JavaScript served directly by the Spring Boot application. The frontend is organized into ES6 modules: api.js handles all fetch calls, dom.js centralizes DOM element references, view.js manages view transitions, study.js handles study session state and rendering, edit.js handles edit mode state and rendering, and main.js wires up all event listeners and coordinates module communication.

Deployment
The application is deployed to Railway with a managed PostgreSQL plugin. Local and production configurations are separated using Spring profiles. A GitHub Actions CI/CD pipeline runs backend tests on every push and deploys to Railway only after tests pass on the main branch.

4. Data Model

Deck
    id
    name
    description
    created_at
    updated_at

Card
    id
    deck_id (foreign key to Deck)
    front_text
    back_text
    priority
    created_at
    updated_at

Each deck contains multiple cards through a one-to-many relationship. The priority field persists the card's scheduling position across study sessions.

5. Technology Stack

Backend
    Java 21
    Spring Boot 4
    Spring Data JPA
    Bean Validation (Jakarta)
    SLF4J logging

Database
    PostgreSQL

Frontend
    HTML / CSS / JavaScript (ES6 modules)

Build and Dependency Management
    Maven Wrapper

Testing
    JUnit 5
    Mockito
    Spring Boot Test (MockMvc)

Deployment and CI/CD
    Railway (hosting and managed PostgreSQL)
    GitHub Actions (CI testing and CD pipeline)

6. Custom Data Structures

Four custom data structures were implemented from scratch without using Java or JavaScript built-in collection equivalents for core logic.

MinHeap (Java)
A binary min-heap backed by an array. Used by the study session to determine which card is due next. Supports insert (hole method, percolate-up), deleteMin (hole method, percolate-down), and O(n) heap construction using Floyd's buildHeap algorithm. Includes a dynamic array resize when the backing array reaches capacity.

CardHashMap (Java)
A hash table using separate chaining for collision resolution. Used to retrieve full card data by ID during a study session. Table size is set to the next prime greater than or equal to twice the number of cards, keeping the load factor around 0.5.

NavigationStack (JavaScript)
A cursor-based browsable history structure used in study mode. Unlike a pure LIFO stack, it supports navigating backward and forward through previously viewed cards without affecting the scheduler. The live edge tracks the most recently fetched card.

CardViewSet (JavaScript)
A hash set with separate chaining that tracks unique card IDs flipped to the answer side during a session. Used to power the session coverage display. Bucket count is rounded up to the nearest power of two above the deck size.

7. API Reference

Deck Endpoints

    GET /api/decks
    Returns a summary list of all decks (id, name, description, card count).

    GET /api/decks/{id}
    Returns a specific deck with its full list of associated cards.

    POST /api/decks
    Creates a new deck. Requires a non-empty name.

    PUT /api/decks/{id}
    Updates a deck's name and description.

    DELETE /api/decks/{id}
    Deletes a deck and all its associated cards.

Card Endpoints

    POST /api/decks/{deckId}/cards
    Creates a new card within a deck. Requires non-empty frontText and backText. Priority is assigned automatically based on the current deck size.

    PUT /api/decks/{deckId}/cards/{cardId}
    Updates a card's frontText and backText. The card must belong to the specified deck.

    DELETE /api/decks/{deckId}/cards/{cardId}
    Deletes a card. The card must belong to the specified deck.

Study Endpoints

    GET /api/study/{deckId}/start
    Builds a new study session for the deck and returns the first card due along with the total card count.

    POST /api/study/{deckId}/next
    Accepts the card just viewed (cardId, priority, markForReview), reschedules it in the heap, persists the updated priority to the database, and returns the next card due.

8. Sprint Summary

Sprint 1 established the project foundation. The Spring Boot project was initialized with PostgreSQL integration, the Deck and Card entities were defined, Spring Data JPA repositories were wired up, and the first two read endpoints were implemented with basic controller tests.

Sprint 2 built the complete vertical slice. A service layer was introduced, DTOs separated API responses from entities, and full CRUD support was added for decks and cards. A browser-based frontend was added with study mode and edit mode. Test coverage was expanded across controllers and services.

Sprint 3 implemented the four custom data structures and integrated them into the study experience. MinHeap and CardHashMap replaced the previous sequential card traversal with priority-based scheduling. NavigationStack and CardViewSet were added on the frontend to support browsable history and session coverage tracking. Two new study endpoints were added and the full scheduling workflow was wired end to end.

Sprint 4 focused on deployment, configuration, and code quality. Spring profiles separated local and production database settings. The application was deployed to Railway with a managed PostgreSQL database. A GitHub Actions CI/CD pipeline was added so that tests must pass before any deployment to main runs. A code quality pass addressed defensive guards, dead code, exception handling, and test coverage gaps.

9. Project Status

The application is complete and deployed. All planned features have been implemented across the four sprints. The study session correctly surfaces cards in scheduler-determined order, persists priority updates to the database between sessions, and responds to the mark-for-review flag. Edit mode supports full deck and card management. The CI/CD pipeline runs on every push to main.
