# Flashcard Study Application

A web-based study tool for organizing flashcards into decks and reviewing them through a priority-based scheduling algorithm. Built with Spring Boot and PostgreSQL, served as a single-page application using plain HTML, CSS, and JavaScript.

## Features

- User accounts (signup, login, logout); each user sees and studies only their own decks
- Create, edit, and delete decks and cards through a browser-based interface
- Study mode presents cards in priority order, scheduled in the browser; progress is saved in the background
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

This starts five containers: PostgreSQL, RabbitMQ, the deck service, the user service, and an nginx gateway. The gateway serves the frontend, forwards `/api/users/` requests to the user service and all other `/api/` requests to the deck service; it is the only port exposed to the network. The RabbitMQ management dashboard is at [http://localhost:15672](http://localhost:15672) (login `flashcards` / `flashcards`), reachable from this machine only. The database starts empty: `db/init.sql` creates each service's schema and login, and the deck service seeds sample decks on first run.

To stop: `Ctrl+C`, or `docker compose down`. To also wipe the database and RabbitMQ's queues: `docker compose down -v`.

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
├── gateway/                  nginx image: serves frontend, routes /api/ to services
│   ├── Dockerfile            Builds nginx with the frontend and routing built in
│   └── default.conf.template Routes; service addresses filled in from environment variables
├── services/                 One folder per microservice
│   ├── pom.xml               Parent build: shared versions, builds all modules
│   ├── common/               Shared library: JWT validation and event classes (contracts only, no business logic)
│   ├── deck-service/         Spring Boot: decks, cards, study mode
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/java/    Controllers, services, models, messaging
│   │       └── test/         JUnit and MockMvc tests
│   └── user-service/         Spring Boot: accounts, signup, login
├── db/init.sql               Database setup: one schema and login per service
├── docker-compose.yml        Local environment: postgres, rabbitmq, services, gateway
└── .github/workflows/        CI/CD pipeline
```

## Documentation

This project is being restructured into a multi-service application. The original single-app version, including its BRD and sprint notes, is preserved in the [v1.0-dsa-final release](https://github.com/SeanMPeake/flashcards/releases/tag/v1.0-dsa-final).

## How Study Works

Study runs in the browser. Opening a deck loads all its cards once; the card with the lowest priority comes next, and after each review its priority grows by the deck size (or half the deck size when marked for review). No server call is needed between cards, so the deck service keeps no study state.

New priorities are saved to the deck service in batches (every 5 cards and when leaving study). If the server can't be reached, studying continues and the save is retried with exponential backoff, even after leaving study. The server never lowers a stored priority (it checks and writes in one atomic update), so late or retried saves, simultaneous saves, and two tabs studying the same deck all merge correctly.

## Study Events

Each saved batch also produces a `StudyProgressSaved` event (cards reviewed and flagged, grouped by study session), published to the RabbitMQ fanout exchange `study.events`. Each consuming service binds its own queue to it.

The event is written to an outbox table in the same database transaction as the priorities, and a background relay sends waiting events to RabbitMQ every two seconds, deleting each once the broker confirms it. If RabbitMQ is down, saving and studying are unaffected: events wait in the outbox and are sent when it is back. Delivery is at-least-once, so consumers skip events whose `eventId` they have already processed.

Events are not stored for replay: a consumer receives events from the moment its queue first exists.

The frontend also uses two small custom structures: **NavigationStack** (browsable prev/next history) and **CardViewSet** (unique cards seen this session). The original Java MinHeap and CardHashMap from the data structures course are preserved in the [v1.0-dsa-final release](https://github.com/SeanMPeake/flashcards/releases/tag/v1.0-dsa-final).
