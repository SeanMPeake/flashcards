# Sprint 4

## Goal

- Deploy the application to a cloud hosting provider, establish a clean separation between local and production configuration, and add a continuous deployment pipeline through GitHub Actions so that every push to the main branch is tested before being released.

## User Stories

- As a developer, I want the application deployed to a live server so that it can be demonstrated without running locally.
- As a developer, I want local and production database configuration separated so that credentials are never committed to version control.
- As a developer, I want automated tests to run and gate deployment on every push to main so that broken code is never deployed.

## Work Completed

### Deployment Configuration

- Spring profiles were introduced to separate local and production database settings. The base application.properties retains shared non-sensitive configuration (JPA dialect, DDL mode, logging suppression). Two profile-specific files were added:

    - application-local.properties holds local PostgreSQL credentials (host, port, database name, username, password). This file is listed in .gitignore and is never committed to the repository.

    - application-prod.properties reads connection details from environment variables injected by the hosting platform at runtime. The datasource URL is constructed from SPRING_DATASOURCE_URL, with username and password read from SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD. The server port is read from PORT with a default fallback of 8080.

- The active profile is controlled by the SPRING_PROFILES_ACTIVE environment variable. Setting it to local picks up the local credentials file; setting it to prod uses the environment variable references. No code changes are needed to switch between environments.

### Railway Deployment

- The application is deployed on Railway using a PostgreSQL plugin attached to the same project. Railway automatically provisions the database and generates connection credentials. These are referenced in the flashcards service as Railway variable references pointing to the Postgres plugin, then exposed to the Spring Boot application through the SPRING_DATASOURCE_* environment variables described above.

- The repository root directory is set to backend in Railway so that the Spring Boot project is detected correctly from the monorepo structure. The SPRING_PROFILES_ACTIVE variable is set to prod in the Railway service environment. On startup, the DataLoader CommandLineRunner seeds three decks with twelve cards each if no data is present.

- A public domain was generated for the deployed service through Railway's Settings panel after the application started successfully.

### CI/CD Pipeline

- The existing CI workflow already ran backend tests on push to main, develop, and feature branches. A deploy job was added to the same workflow file to complete the pipeline.

- The deploy job depends on the backend job through the needs field, which prevents deployment from running if any test fails. It is also gated to run only on push events to the main branch, so test-only runs on feature branches do not trigger a deployment.

- The deploy job installs the Railway CLI and runs railway up with the service ID and project ID supplied as GitHub Actions secrets. A Railway project-level token (distinct from a personal account token) is stored as a secret and passed to the CLI through the RAILWAY_TOKEN environment variable. The --detach flag is used so the job does not wait for Railway to finish building before completing.

- Three repository secrets are required: RAILWAY_TOKEN, RAILWAY_PROJECT_ID, and RAILWAY_SERVICE_ID.

### Code Quality Pass

- A focused review of the codebase was completed to address defensive programming, dead code, and exception handling consistency. Changes made:

    - Fail-fast guards were added to all custom data structure methods. MinHeap.insert and CardHashMap.insert throw IllegalArgumentException on null card IDs. StudySession.reschedule and findCard validate their inputs before delegating to the underlying structures. The MinHeap constructor and CardHashMap constructor both check for a null cards list.

    - The MinHeap insert method was updated to detect when the backing array is full and double its capacity using Arrays.copyOf before inserting. This addressed a real bounds issue in the session rebuild path where reschedule is called before any deleteMin, putting the heap at N+1 elements on a freshly built N-element array.
    
    - Dead setter methods were removed from Card and Deck (setCreatedAt, setUpdatedAt, and setCards on Deck). These were never called and their presence implied mutability that was not intended.

    - Null guards were added to the Card and Deck constructors. Card rejects null deck, null or blank frontText, and null or blank backText. Deck rejects null or blank name.

    - The GlobalExceptionHandler was extended with handlers for IllegalArgumentException (400) and IllegalStateException (500 with logging). A SLF4J logger was added and the generic catch-all handler now logs the full exception before returning a 500 response.

    - CardServiceImpl was refactored to extract a private requireCardInDeck helper used by both updateCard and deleteCard, eliminating duplicated ownership validation logic.

    - The buildResponse method in StudyServiceImpl was updated to throw IllegalStateException if findCard returns null, replacing a silent null dereference with an explicit failure.

### Test Suite Improvements

- MinHeapTest was updated so the cards helper assigns sequential IDs via reflection, matching the null ID guard added to MinHeap.insert. A direct insert call that previously passed null as the card ID was updated to use a valid ID.

- DeckServiceImplTest was updated to use deck.getCards().add(card) instead of the removed setCards setter. The redundant setCards(List.of()) call for the empty deck test was removed.

- StudyControllerTest and StudyServiceImplTest were updated to reflect the StudyStartResponse return type from startSession. Stubs were updated to return new StudyStartResponse(card, 12), and JSON path assertions were updated to $.card.id, $.card.frontText, and $.totalCards.

- Two new tests were added to StudyServiceImplTest: nextCardPersistsUpdatedPriority verifies that the rescheduled card's new priority is saved to the database using ArgumentCaptor, and nextCardThrowsWhenCardMissingFromSessionMap verifies that a null result from findCard produces an IllegalStateException.

- StudySessionTest was strengthened: findCardReturnsCorrectCard now asserts all three card fields (frontText, backText, priority) rather than only frontText.

## Challenges

- Railway's PostgreSQL plugin injects DATABASE_URL in postgres:// format, while the JDBC driver requires jdbc:postgresql://. The scheme mismatch caused the driver to reject the URL without a clear error message. The solution was to avoid using DATABASE_URL directly and instead construct the JDBC URL from individual connection components exposed as separate Spring datasource variables.

- Railway distinguishes between personal account tokens and project-level tokens. The Railway CLI used in GitHub Actions requires a project token, not an account token. Using an account token produces an invalid token error even if the token value is correct.

- The Railway service Variables tab does not automatically inherit environment variables from the PostgreSQL plugin in the same project. Each variable needed to be added as an explicit reference to the Postgres service using Railway's ${{ServiceName.VARIABLE}} reference syntax before Spring Boot could read them.

## Decisions Made

- Spring profiles were chosen over a single properties file with environment variable fallbacks because profiles make the local and production configurations explicit and independent. A developer running locally sets SPRING_PROFILES_ACTIVE=local and the file handles the rest; Railway sets it to prod and the environment variable references take over.

- application-local.properties is gitignored rather than committed with placeholder values. This prevents local credentials from appearing in version control history, even if only default values.

- The CD job was added to the existing ci.yml workflow rather than a separate cd.yml file. Keeping them together makes the dependency between testing and deployment explicit in one place and reduces workflow configuration overhead.

- Railway's native GitHub auto-deploy was disabled in favor of the GitHub Actions pipeline so that deployment is always gated by the test suite. The Actions workflow is the single source of truth for what gets deployed and when.
