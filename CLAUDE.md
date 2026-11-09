# Claude Code Instructions

## General Behavior
- Do not make any edits to files without explicit approval
- Present proposed changes for review before implementing
- Do not infer or assume requirements beyond what is explicitly discussed
- Do not add extra features, refactors, or improvements unless asked
- Ask clarifying questions if a request is ambiguous rather than guessing

## Code Changes
- Show a diff or summary of what you plan to change before changing it
- Make one logical change at a time, not sweeping multi-file edits
- Do not rename variables, restructure files, or reformat code unless specifically requested
- Do not flag incidental optimizations or refactors while implementing new features

## Project Context
- This is a student project — keep implementations straightforward and readable
- Prefer clarity over cleverness
- Do not introduce new dependencies or libraries without discussion

## Project Architecture
- SDEV 4310 final project: restructuring into four services — User, Deck/Card
  (current backend/), Review Insights, Analytics — communicating via RabbitMQ events,
  with one PostgreSQL instance and a separate schema and database role per service.
- Services communicate only over the network (HTTP or messages). Never import one
  service's classes into another.
- Shared code goes only in a `common` module: event classes and JWT validation,
  no business logic. Event changes are additive only (never rename or remove fields).
- The nginx gateway (gateway/nginx.conf) is the single entry point; each new service
  gets a location block there rather than CORS configuration.
- When proposing a design choice, state which quality attribute ("ility") it gains
  and which it costs — the course grades on this.

## Git Workflow
- Work on `feature/<name>` branches; CI only runs on that prefix. Never commit
  directly to main — changes go through a pull request.
- The Railway deploy job is paused (`if: false` in ci.yml). Do not re-enable it
  without discussion.

## Local Environment
- Run the app with `docker compose up --build` from the repo root; open localhost:8080.
- Say before running any Docker or other command that reaches outside the repo.

## Communication
- Flag anything that seems like it could break existing functionality
- If something I ask for conflicts with existing code, point it out before proceeding