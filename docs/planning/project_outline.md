Project Outline / BRD-lite (v0.2 aligned to 3330 + 3340) 

1) Project Summary 

This project is a full-stack flashcard study web application that allows users to create decks, add cards, and review content for learning purposes. It is developed under an Agile/Iterative SDLC process and will intentionally implement non-trivial data structures and algorithmic reasoning for complexity analysis. 

2) Business Problem and Value (this is the key for 3330) 

Problem: Students often study inefficiently (reviewing easy material too often and hard material too rarely), and they lack lightweight tooling that prioritizes what to study next. 
Value: The app improves study efficiency by prioritizing cards due for review and tracking basic progress, enabling users to focus on the most time-sensitive or least-mastered material. 

This single section helps you avoid the “CRUD-only” penalty because it frames the app as process improvement + decision logic, not just storage. 

3) Scope 

MVP / Core Features (app must do this) 

    Deck management: create/list/rename/delete decks 

    Card management: add/edit/delete cards in a deck 

    Study mode: flip front/back, next card, basic session flow 

    Persistence: database-backed data survives refresh/restart 

    Anonymous user identity: cookie-based ownerKey (no full auth) 

Planned Core (required for 3340 and to avoid CRUD-only) 

    Spaced repetition scheduling / prioritization using a min-heap / priority queue 

    Cards have a “due” concept (even if simple initially) 

    Study mode selects the next card via heap operations (not linear scan) 

    Hash-based structure (hash map and/or set) for efficient lookup/support operations 

        e.g., fast card lookup by ID, membership checks, or update coordination with scheduling 

    Basic study tracking sufficient to justify business value (e.g., last reviewed, correct/incorrect count, next due) 

Optional (only if time late semester) 

    Import/export decks (JSON) 

    AI “Explain this card” helper 

    Search/filter 

    Stats dashboard beyond basics 

Out of Scope 

    Full authentication (email verification, resets, 2FA, multi-device sync) 

    Production-grade security hardening beyond safe defaults 

    Heavy UI/UX polish beyond basic usability 

4) Constraints and Assumptions 

    Solo developer, ~5–10 hours per sprint 

    Must use version control with incremental commits across sprints (3330) 

    Must show testing evidence (3330) 

    Must eventually deploy (3330) — timing depends on sprint plan 

    Must produce 3–5 page technical analysis report and presentation (3340) 

5) Success Criteria (maps to grading) 

For 3330 

    Business requirements are documented and traceable to implemented features (20%) 

    Demonstrable incremental progress per sprint (Git commits + sprint notes) 

    Testing evidence exists (manual checklist and/or unit tests) 

    Working deployed application by the end 

For 3340 

    At least two non-trivial data structures are used intentionally (heap + hash-based) 

    Clear complexity analysis for key operations 

    Cyclomatic complexity computed (overall + most complex function) 

    Performance reflection: what breaks at 100× scale + first optimization target 

6) Sprint 1 Goal (keep it realistic) 

Deliver a working vertical slice locally: 
Create deck → add card → study deck (flip + basic navigation) → persisted in DB. 
Also produce the initial documentation artifacts (below). 

 

Documentation strategy that satisfies 3330 without eating the sprint 

Because “business requirements gathering & documentation = 20%,” you do not want a giant BRD. You want small docs that are clearly traceable to work. 

Create /docs/ with: 

    BRD-lite / Project Outline (the document above) 

    Requirements + User Stories (requirements.md) 

        8–12 user stories total (not 40) 

        Acceptance criteria only for Sprint 1 stories 

    Sprint Log (sprint-01.md, sprint-02.md, …) 

        Goal, backlog items, what changed, what was done, blockers, next sprint 

That trio will read like “real process discipline” and directly supports the 20% grade category. 

 

Sprint 1 backlog (minimal, fits your time budget) 

Docs (1–1.5 hrs) 

    Project Outline v0.2 (above) 

    6–8 initial user stories + Sprint 1 acceptance criteria 

    Sprint 1 plan doc (goal + tasks) 

Setup (2 hrs) 

    Repo scaffold + basic folder structure 

    Express + TS running 

    DB + migrations (Deck/Card schema) 

Vertical slice (2–4 hrs) 

    Deck create/list 

    Card add/list 

    Study mode UI: flip + next 

    Persistence verified 

Testing evidence (30–60 mins) 

    Minimal manual test checklist or 2–3 unit tests for pure functions 
    (Counts toward the 10% testing category without ballooning scope.) 

 