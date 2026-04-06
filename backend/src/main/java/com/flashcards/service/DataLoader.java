package com.flashcards.service;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// @Configuration is needed here to register the loadData @Bean.
// DataLoader lives in the service package because it seeds domain data,
// not infrastructure or application configuration.
@Configuration
public class DataLoader {

    // Seeds starter data for development and testing when the application starts.
    @Bean
    CommandLineRunner loadData(DeckRepository deckRepository, CardRepository cardRepository) {
        return args -> {
            // Only seed data if the database is empty so the same records are not duplicated
            // each time the application restarts.
            if (deckRepository.count() == 0) {
                Deck javaDeck = new Deck("Java Basics", "Core Java concepts and syntax");
                Deck sdlcDeck = new Deck("SDLC Terms", "Software development life cycle concepts");
                Deck sqlDeck = new Deck("SQL Basics", "Relational database and query fundamentals");

                deckRepository.saveAll(List.of(javaDeck, sdlcDeck, sqlDeck));

                cardRepository.saveAll(List.of(
                        // Java Basics — 12 cards
                        new Card(javaDeck, "What is a class?", "A blueprint for creating objects, defining their state and behavior.", 1),
                        new Card(javaDeck, "What is an object?", "An instance of a class with its own state.", 2),
                        new Card(javaDeck, "What is inheritance?", "A mechanism where one class acquires the properties and methods of another.", 3),
                        new Card(javaDeck, "What is polymorphism?", "The ability of a method or object to take multiple forms.", 4),
                        new Card(javaDeck, "What is encapsulation?", "Restricting direct access to object fields by exposing them through methods.", 5),
                        new Card(javaDeck, "What is an interface?", "A contract specifying methods a class must implement, without providing the implementation.", 6),
                        new Card(javaDeck, "What is the difference between == and .equals()?", "== compares references; .equals() compares object content.", 7),
                        new Card(javaDeck, "What is a constructor?", "A special method called when an object is created, used to initialize its state.", 8),
                        new Card(javaDeck, "What does 'static' mean?", "A static member belongs to the class itself rather than to any instance.", 9),
                        new Card(javaDeck, "What is an abstract class?", "A class that cannot be instantiated and may contain abstract methods without implementations.", 10),
                        new Card(javaDeck, "What is the JVM?", "The Java Virtual Machine — executes compiled Java bytecode on any platform.", 11),
                        new Card(javaDeck, "What is a checked exception?", "An exception that must be declared or handled at compile time.", 12),

                        // SDLC Terms — 12 cards
                        new Card(sdlcDeck, "What does SDLC stand for?", "Software Development Life Cycle.", 1),
                        new Card(sdlcDeck, "What is Agile?", "An iterative development methodology that delivers working software in short cycles.", 2),
                        new Card(sdlcDeck, "What is a sprint?", "A fixed-length iteration in Agile, typically one to four weeks.", 3),
                        new Card(sdlcDeck, "What is a user story?", "A short description of a feature from the perspective of the end user.", 4),
                        new Card(sdlcDeck, "What is version control?", "A system that tracks changes to code over time and supports collaboration.", 5),
                        new Card(sdlcDeck, "What is a code review?", "A process where developers examine each other's code to catch issues and share knowledge.", 6),
                        new Card(sdlcDeck, "What is CI/CD?", "Continuous Integration and Continuous Delivery — automating build, test, and deployment pipelines.", 7),
                        new Card(sdlcDeck, "What is a pull request?", "A request to merge a branch into another, typically reviewed before merging.", 8),
                        new Card(sdlcDeck, "What is technical debt?", "The cost of shortcuts taken during development that must be addressed later.", 9),
                        new Card(sdlcDeck, "What is refactoring?", "Restructuring existing code without changing its external behavior.", 10),
                        new Card(sdlcDeck, "What is a retrospective?", "An Agile ceremony where the team reflects on what went well and what to improve.", 11),
                        new Card(sdlcDeck, "What is the difference between unit and integration testing?", "Unit tests verify individual components in isolation; integration tests verify components working together.", 12),

                        // SQL Basics — 12 cards
                        new Card(sqlDeck, "What does SQL stand for?", "Structured Query Language.", 1),
                        new Card(sqlDeck, "What is a primary key?", "A column or set of columns that uniquely identifies each row in a table.", 2),
                        new Card(sqlDeck, "What is a foreign key?", "A column that references the primary key of another table, establishing a relationship.", 3),
                        new Card(sqlDeck, "What does SELECT do?", "Retrieves data from one or more tables.", 4),
                        new Card(sqlDeck, "What does WHERE do?", "Filters rows returned by a query based on a condition.", 5),
                        new Card(sqlDeck, "What is a JOIN?", "Combines rows from two or more tables based on a related column.", 6),
                        new Card(sqlDeck, "What is the difference between INNER JOIN and LEFT JOIN?", "INNER JOIN returns only matching rows; LEFT JOIN returns all rows from the left table plus matches from the right.", 7),
                        new Card(sqlDeck, "What does GROUP BY do?", "Aggregates rows that share a value in the specified column.", 8),
                        new Card(sqlDeck, "What is an index?", "A data structure that speeds up queries by allowing faster row lookups.", 9),
                        new Card(sqlDeck, "What does NULL represent?", "The absence of a value — not zero or empty string.", 10),
                        new Card(sqlDeck, "What is a transaction?", "A sequence of operations treated as a single unit — either all succeed or all are rolled back.", 11),
                        new Card(sqlDeck, "What does DISTINCT do?", "Removes duplicate values from query results.", 12)
                ));
            }
        };
    }
}
