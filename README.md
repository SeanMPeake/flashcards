# Flashcard Study Application

A web-based study tool for organizing flashcards into decks and reviewing them through a priority-based scheduling algorithm. Built with Spring Boot and PostgreSQL, served as a single-page application using plain HTML, CSS, and JavaScript.

## Features

- Create, edit, and delete decks and cards through a browser-based interface
- Study mode presents cards in scheduler-determined order using a min-heap algorithm
- Mark a card for earlier re-review during a session
- Navigate backward and forward through previously viewed cards without affecting the scheduler
- Session coverage display tracks unique cards seen per session
- Data persists across sessions via PostgreSQL

## Tech Stack

- **Backend:** Java 21, Spring Boot 4, Spring Data JPA
- **Database:** PostgreSQL
- **Frontend:** HTML, CSS, JavaScript (ES6 modules)
- **Build:** Maven Wrapper
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **CI/CD:** GitHub Actions, Railway
- **Local environment:** Docker Compose, nginx (gateway)

## Running Locally

**Prerequisites:** Docker Desktop (running)

From the repository root:

```
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080).

This starts four containers: PostgreSQL, the deck service, the user service, and an nginx gateway. The gateway serves the frontend, forwards `/api/users/` requests to the user service and all other `/api/` requests to the deck service; it is the only port exposed. The database starts empty: `db/init.sql` creates each service's schema and login, and the deck service seeds sample decks on first run.

To stop: `Ctrl+C`, or `docker compose down`. To also wipe the database: `docker compose down -v`.

After pulling changes that alter a table (for example a new required column), run `docker compose down -v` once before starting again: tables are created and updated automatically at startup, but a required column cannot be added to rows that already exist.

In local development every account gets three sample decks the first time it opens its deck list (the `dev` profile set in `docker-compose.yml`). Real deployments run without that profile, so new users start with no decks.

## Running Tests

From the `services` directory, all services:

```
./mvnw test
```

One service (plus any shared modules it depends on):

```
./mvnw -pl user-service -am test
```

## Project Structure

```
flashcards/
├── frontend/                 HTML, CSS, JS modules (served by the gateway)
├── gateway/                  nginx config: serves frontend, routes /api/ to services
├── services/                 One folder per microservice
│   ├── pom.xml               Parent build: shared versions, builds all modules
│   ├── common/               Shared library: JWT validation (contracts only, no business logic)
│   ├── deck-service/         Spring Boot: decks, cards, study mode
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/java/    Controllers, services, models, custom data structures
│   │       └── test/         JUnit and MockMvc tests
│   └── user-service/         Spring Boot: accounts, signup, login
├── db/init.sql               Database setup: one schema and login per service
├── docker-compose.yml        Local environment: postgres, services, gateway
└── .github/workflows/        CI/CD pipeline
```

## Documentation

This project is being restructured into a multi-service application. The original single-app version, including its BRD and sprint notes, is preserved in the [v1.0-dsa-final release](https://github.com/SeanMPeake/flashcards/releases/tag/v1.0-dsa-final).

## Custom Data Structures

Four data structures implemented from scratch:

- **MinHeap** (Java) — priority queue for study session scheduling
- **CardHashMap** (Java) — hash table with separate chaining for card lookup by ID
- **NavigationStack** (JavaScript) — cursor-based browsable history for prev/next navigation
- **CardViewSet** (JavaScript) — hash set tracking unique cards flipped during a session
