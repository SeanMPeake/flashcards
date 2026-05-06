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

## Running Locally

**Prerequisites:** Java 21+, Maven, PostgreSQL running locally with a database named `flashcards`

Create `backend/src/main/resources/application-local.properties` (this file is gitignored):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flashcards
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.show-sql=true
```

Then from the `backend` directory:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

## Running Tests

From the `backend` directory:

```
./mvnw test
```

## Project Structure

```
flashcards/
├── backend/                  Spring Boot application
│   └── src/
│       ├── main/
│       │   ├── java/         Controllers, services, models, custom data structures
│       │   └── resources/
│       │       └── static/   Frontend (HTML, CSS, JS modules)
│       └── test/             JUnit and MockMvc tests
├── docs/                     BRD, sprint notes, project log
└── .github/workflows/        CI/CD pipeline
```

## Documentation

- [Project Log](docs/project-log.md) — full project overview, architecture, API reference, and sprint summaries
- [BRD](docs/BRD.md) — business requirements and success criteria
- [Sprint Notes](docs/sprints/) — detailed notes for each sprint including decisions made and challenges

## Custom Data Structures

Four data structures implemented from scratch:

- **MinHeap** (Java) — priority queue for study session scheduling
- **CardHashMap** (Java) — hash table with separate chaining for card lookup by ID
- **NavigationStack** (JavaScript) — cursor-based browsable history for prev/next navigation
- **CardViewSet** (JavaScript) — hash set tracking unique cards flipped during a session
