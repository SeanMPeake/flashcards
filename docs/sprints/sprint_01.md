Sprint 1

Goal

    Define the project scope and architecture for the flashcards application and establish the initial backend foundation. This included planning the application structure, designing the core data model, setting up the development environment, and implementing the first working API endpoint connected to a database.

Work Completed

    During the first sprint, significant time was spent planning the project before implementation began. This included defining the purpose of the application, outlining the system architecture, and drafting initial documentation such as the project overview and backlog structure. Key design decisions were made regarding the technology stack, backend framework, database choice, and overall application structure.

    After planning was complete, the backend project was initialized using Spring Boot with a Maven wrapper. PostgreSQL was installed and configured locally, and the application was connected to the database using Spring Data JPA. The initial database schema was designed with two primary entities: Deck and Card, with a one-to-many relationship between them.

    Entity models and repository interfaces were implemented, allowing Spring Data JPA to automatically generate database access logic. A startup data loader was created to seed initial deck and card data into the database. Finally, the /api/decks endpoint was updated to return real database-backed data instead of hardcoded values, confirming that the backend, persistence layer, and API routing were working together correctly.

Challenges

    The primary challenge during this sprint was the initial setup and architecture decisions required before implementation could begin. Since the project requirements were intentionally open-ended, time was required to determine the application's scope, select the technology stack, and design the initial system structure.

    Additional challenges included configuring the development environment, learning the Spring Boot framework and dependency structure, integrating PostgreSQL with the application, and establishing the correct relationships between entities in the database model.

Decisions Made

    Several foundational design decisions were made during this sprint. The backend was implemented using Java with Spring Boot instead of Node.js in order to align with existing coursework and provide a stronger typed backend environment. PostgreSQL was selected as the database to allow the project to be deployable in a live environment later if needed.

    The database schema was intentionally kept simple, consisting of Deck and Card tables with a one-to-many relationship. Authentication and user ownership of decks were deferred to keep the project scope manageable. The scheduling logic for card review will be implemented later using a min-heap priority queue, with the database currently storing a basic priority value for each card.

Next Steps

    Next steps for the project include expanding the API to support additional endpoints such as retrieving individual decks and cards, introducing a service layer to separate business logic from the controller layer, and beginning implementation of the card scheduling algorithm using the planned heap-based approach.

    Future work will also include refining the database model as needed, adding CRUD operations for decks and cards, and continuing to build out the application structure in preparation for the algorithm-focused components of the project.
