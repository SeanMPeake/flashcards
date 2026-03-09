Flashcards Study Application
Spring 2026 Software Development Project

1. Project Overview

Flashcards is a web-based study application that allows users to organize flashcards into decks and review them through a backend API. The system is being developed iteratively using Spring Boot and PostgreSQL. Later sprints will introduce scheduling logic to prioritize cards for review using a heap-based priority system.

2. Problem Statement

Students and self-learners often need a simple way to organize study material and review it repeatedly. Many flashcard tools focus primarily on storage rather than structured review. This project aims to support more intentional study behavior by combining deck management with algorithm-driven scheduling to determine which cards should be reviewed next.

3. System Goals

The system should allow users to create and manage decks and cards, retrieve study content through API endpoints, and support review prioritization using a min-heap priority queue. The project also demonstrates backend architecture, database integration, and the practical use of data structures and algorithms within an application.

4. Core Features

Planned core features include:

* Create and manage flashcard decks
* Create, edit, and delete cards within a deck
* Retrieve decks and cards through REST API endpoints
* Schedule card review order using a priority-based algorithm
* Support study sessions that return cards in algorithm-determined order

These features will be implemented incrementally across project sprints.

5. Architecture Overview

Backend
Spring Boot REST API providing endpoints for deck and card management.

Database
PostgreSQL database used to store deck and card data.

Core Entities
Deck
Card

Application Layers

Controller – API endpoint definitions
Repository – database access through Spring Data JPA
Service – business logic and scheduling behavior (planned)

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

Database
PostgreSQL

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
* Additional endpoints and data operations

9. Current System Status

The backend project has been initialized and connected to PostgreSQL. The initial Deck and Card entities have been implemented along with repository interfaces for database interaction. The database schema is automatically generated through JPA, and starter data is seeded at application startup. Two API endpoints are currently available:

GET /api/decks  
Returns all decks and their associated cards.

GET /api/decks/{id}  
Returns a specific deck and its cards. A 404 response is returned if the deck does not exist.

Basic controller-level tests have been implemented to verify these endpoints using mocked repository responses.

10. API Endpoints

The backend exposes a REST API for interacting with flashcard decks and cards. Endpoints will be expanded incrementally as new functionality is implemented.

Current Endpoints

    GET /api/decks
    Returns all decks currently stored in the system, including their associated cards.

    GET /api/decks/{id}
    Returns a specific deck and its cards.

Planned Endpoints


    POST /api/decks
    Creates a new flashcard deck.

    PUT /api/decks/{id}
    Updates an existing deck.

    DELETE /api/decks/{id}
    Deletes a deck and its associated cards.

    GET /api/cards/{id}
    Returns a specific flashcard.

    POST /api/cards
    Creates a new card within a deck.

    PUT /api/cards/{id}
    Updates an existing card.

    DELETE /api/cards/{id}
    Deletes a flashcard.

    GET /api/study/{deckId}
    Returns the next card to review for a given deck based on the scheduling algorithm.

