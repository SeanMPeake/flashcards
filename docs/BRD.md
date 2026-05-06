# Business Requirements Document (BRD)

**Project Name:** Flashcard Study Application  
**Version:** 1.0  
**Date:** Spring 2026  

---

## 1. Executive Summary

The Flashcard Study Application is a web-based system designed to help users organize and review study material efficiently. The application allows users to create decks of flashcards, manage card content, and study materials in a structured manner.

The goal of the system is to provide a lightweight, focused study tool that improves review efficiency and supports scalable study workflows.

---

## 2. Problem Statement

Many learners rely on informal or inefficient study methods that do not prioritize material based on difficulty or review frequency. Existing tools may be overly complex, feature-heavy, or not aligned with focused study workflows.

There is a need for a streamlined, organized flashcard system that:

- Allows structured content management  
- Supports focused review sessions  
- Scales effectively as study material grows  

---

## 3. Business Objectives

The application aims to:

1. Provide an organized system for managing study decks and flashcards.  
2. Enable users to efficiently add, modify, and review learning material.  
3. Support structured review workflows that can scale with increased data size.  
4. Deliver a professional-quality web application demonstrating structured development practices.  

---

## 4. Scope

### 4.1 In Scope

The system will include:

- Deck creation and management  
- Flashcard creation within decks  
- Viewing and listing decks  
- Persistent data storage  
- Priority-based study session scheduling using a min-heap algorithm  
- Card edit mode supporting create, update, and delete operations  
- Cloud deployment with a publicly accessible URL  

### 4.2 Out of Scope

The following are not included in the initial scope:

- Advanced authentication systems (email verification, multi-factor authentication)  
- Social sharing features  
- Real-time collaboration  
- Mobile-native applications  
- Enterprise-level security hardening  

---

## 5. Functional Requirements

FR-01: The system shall allow a user to create a new deck.  
FR-02: The system shall allow a user to view a list of existing decks.  
FR-03: The system shall allow a user to add flashcards to a deck.  
FR-04: The system shall persist decks and cards in a database.  
FR-05: The system shall present cards during study sessions in priority order determined by a min-heap scheduling algorithm, and shall allow a user to mark a card for earlier re-review.  
FR-06: The system shall allow a user to create, update, and delete cards within a deck through an edit mode interface.  
FR-07: The system shall be deployable to a cloud hosting environment and accessible via a public URL.  

---

## 6. Non-Functional Requirements

NFR-01: The application shall maintain clean modular architecture.  
NFR-02: The system shall support data persistence across sessions.  
NFR-03: The system shall remain responsive under increasing dataset size.  
NFR-04: The system shall follow structured development practices aligned with iterative SDLC methodology.  
NFR-05: The codebase shall maintain reasonable modular complexity and readability.  

---

## 7. Success Criteria

The project will be considered successful if:

- Users can create, edit, and delete decks and cards successfully.  
- Data persists reliably across sessions.  
- Study sessions present cards in scheduler-determined order and respond correctly to mark-for-review input.  
- The application is deployed and accessible via a public URL.  
- The codebase demonstrates structured, maintainable implementation across all four sprints.  