Flashcards Study Application
Spring 2026 Software Development Project

1. Project Overview

Flashcards is a web-based study application that allows users to organize flashcards into decks and review them through a browser-based interface backed by a REST API. The system is being developed iteratively using Spring Boot and PostgreSQL. Later sprints will introduce scheduling logic to prioritize cards for review using a heap-based priority system.

2. Problem Statement

Students and self-learners often need a simple way to organize study material and review it repeatedly. Many flashcard tools focus primarily on storage rather than structured review. This project aims to support more intentional study behavior by combining deck management with algorithm-driven scheduling to determine which cards should be reviewed next.

3. System Goals

The system should allow users to create and manage decks and cards, study flashcard content through a browser interface, and support review prioritization using a min-heap priority queue. The project also demonstrates backend architecture, database integration, REST API design, and the practical use of data structures and algorithms within an application.

4. Core Features

Planned core features include:

* Create and manage flashcard decks
* Create, edit, and delete cards within a deck
* Study flashcards through a browser-based interface
* Schedule card review order using a priority-based algorithm
* Support study sessions that return cards in algorithm-determined order

These features are being implemented incrementally across project sprints.

5. Architecture Overview

Backend
Spring Boot REST API providing endpoints for deck and card management.

Database
PostgreSQL database used to store deck and card data.

Frontend
Static HTML, CSS, and JavaScript served by the Spring Boot application. Organized into ES6 modules for API communication, DOM management, view transitions, and study/edit mode logic.

Core Entities
Deck
Card

Application Layers

Controller – API endpoint definitions
Service – business logic, validation, and scheduling behavior (planned)
Repository – database access through Spring Data JPA

6. Data Model

Initial database schema consists of two primary tables:

Deck

* id
* name
* description
* created_at
* updated_at

Card

* id
* deck_id
* front_text
* back_text
* priority
* created_at
* updated_at

Each deck contains multiple cards through a one-to-many relationship.

7. Technology Stack

Backend
Java
Spring Boot
Spring Data JPA
Bean Validation

Database
PostgreSQL

Frontend
HTML / CSS / JavaScript (ES6 modules)

Build / Dependency Management
Maven Wrapper

Development Tools
VS Code
pgAdmin

8. Product Backlog

The product backlog will evolve over the course of the project and will be refined after each sprint. Initial backlog items include:

* Backend project setup
* Database schema and integration
* Deck and card API endpoints
* Service layer implementation
* Scheduling algorithm implementation
* Study session logic
* UI polish and frontend testing (deferred to later sprint)

9. Current System Status

The backend and frontend are both functional. The application supports a full study workflow and a complete edit mode for managing decks and cards through the browser.

The service layer handles all business logic including deck and card CRUD operations, card ownership enforcement, and input validation. A global exception handler returns consistent error responses across all endpoints.

Current API endpoints:

    GET /api/decks
    Returns a summary list of all decks, including name, description, and card count.

    GET /api/decks/{id}
    Returns a specific deck with its full list of associated cards.

    POST /api/decks
    Creates a new deck.

    PUT /api/decks/{id}
    Updates an existing deck's name and description.

    DELETE /api/decks/{id}
    Deletes a deck and all its associated cards.

    POST /api/decks/{deckId}/cards
    Creates a new card within a deck.

    PUT /api/decks/{deckId}/cards/{cardId}
    Updates an existing card's front and back text.

    DELETE /api/decks/{deckId}/cards/{cardId}
    Deletes a card from a deck.

10. API Endpoints

The backend exposes a REST API for interacting with flashcard decks and cards.

Current Endpoints

    GET /api/decks
    Returns a summary list of all decks (id, name, description, card count).

    GET /api/decks/{id}
    Returns a specific deck with its full card list.

    POST /api/decks
    Creates a new deck. Requires a non-empty name.

    PUT /api/decks/{id}
    Updates a deck's name and description.

    DELETE /api/decks/{id}
    Deletes a deck and all its cards.

    POST /api/decks/{deckId}/cards
    Creates a new card within a deck. Requires non-empty frontText and backText.

    PUT /api/decks/{deckId}/cards/{cardId}
    Updates a card's frontText and backText.

    DELETE /api/decks/{deckId}/cards/{cardId}
    Deletes a card. The card must belong to the specified deck.

Planned Endpoints

    GET /api/study/{deckId}
    Returns the next card to review for a given deck based on the scheduling algorithm.
