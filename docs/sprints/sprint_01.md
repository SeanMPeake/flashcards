# Sprint 1

## Goal
- Establish the project foundation by defining the application scope, selecting the technology stack, designing the initial data model, and implementing a functional backend connected to a database.

## Work Completed

- The first sprint focused primarily on project planning and initial system setup. Early work involved defining the purpose of the application, outlining the system architecture, and drafting the initial project documentation and backlog.

- After planning, the backend project was initialized using Spring Boot with the Maven wrapper. PostgreSQL was installed and configured locally, and the application was connected to the database using Spring Data JPA. The initial schema was designed around two core entities: Deck and Card, with a one-to-many relationship.

- Entity models and repository interfaces were implemented to enable database access through Spring Data JPA. A startup data loader was added to seed initial decks and cards for development and testing.

## Two API endpoints were implemented:

- GET /api/decks
    Returns all available decks.

- GET /api/decks/{id}
    Returns a specific deck and its associated cards.

- Basic controller-level tests were introduced using Spring’s testing framework and mocked repositories to verify that these endpoints return successful responses and expected data structures. Exception handling was also added to return a 404 response when a requested deck does not exist.

## Challenges

- A significant portion of the sprint was spent defining the project direction before development could begin. Because the project requirements were intentionally open-ended, time was required to determine the application scope, select the technology stack, and design the initial architecture.

- Additional challenges included configuring the development environment, learning Spring Boot conventions, integrating PostgreSQL with the application, and implementing the initial entity relationships within the database model.

## Decisions Made

- The backend was implemented using Java with Spring Boot rather than Node.js to better align with existing coursework and provide a strongly typed backend environment. PostgreSQL was selected as the database to support potential deployment beyond local development.

- The initial database schema was intentionally kept simple, consisting of Deck and Card tables linked by a one-to-many relationship. Authentication and user ownership of decks were deferred to keep the project scope manageable during early development.

- A min-heap priority queue is planned for the card scheduling algorithm. The current schema includes a basic priority field that will support this logic when the scheduling system is implemented in later sprints.

## Next Steps

- The next sprint will focus on expanding the API, introducing a service layer to separate business logic from controllers, and beginning development of the study workflow for reviewing cards.

- Future work will also include building a minimal user interface for selecting decks and viewing cards, implementing the scheduling algorithm, and expanding test coverage as application logic becomes more complex.
