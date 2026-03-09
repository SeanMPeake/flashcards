Sprint 1

Goal

Establish the project foundation by defining the application scope, selecting the technology stack, designing the initial data model, and implementing a working backend connected to a database.

Work Completed

The first sprint focused heavily on project planning and initial system setup. Early work involved defining the purpose of the application, outlining the system architecture, and drafting the initial project documentation and backlog.

After planning, the backend project was initialized using Spring Boot with a Maven wrapper. PostgreSQL was installed and configured locally, and the application was connected to the database using Spring Data JPA. The initial schema was designed around two core entities: **Deck** and **Card**, with a one-to-many relationship.

Entity models and repository interfaces were implemented to enable database access through Spring Data JPA. A startup data loader was added to seed initial decks and cards for development and testing.

Two API endpoints were implemented:

GET /api/decks
Returns all available decks.

GET /api/decks/{id}
Returns a specific deck and its cards.

Basic controller tests were also introduced to verify that these endpoints return successful responses and expected data structures.

Challenges

A large portion of the sprint was spent defining the project direction before development could begin. Because the project requirements were open-ended, time was required to determine the application scope, choose the technology stack, and design the initial architecture.

Additional challenges included configuring the development environment, becoming familiar with Spring Boot conventions, integrating PostgreSQL with the application, and implementing the initial entity relationships.

Decisions Made

The backend was implemented using **Java with Spring Boot** rather than Node.js to better align with coursework and provide a strongly typed backend environment. **PostgreSQL** was selected as the database to support potential deployment beyond local development.

The initial database schema was intentionally kept simple with **Deck** and **Card** tables linked by a one-to-many relationship. Authentication and user ownership of decks were deferred to keep the project scope manageable.

A **min-heap priority queue** is planned for the card scheduling algorithm, with the current schema storing a basic priority field that will support this logic later.

Next Steps

The next sprint will focus on expanding the API, introducing a service layer to separate business logic from controllers, and beginning development of the study flow for reviewing cards.

Future work will also include building a minimal user interface for selecting decks and viewing cards, implementing the scheduling algorithm, and expanding test coverage as application logic becomes more complex.
