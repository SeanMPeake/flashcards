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
                Deck javaDeck = new Deck("Java Basics", "Core Java review cards");
                Deck sdlcDeck = new Deck("SDLC Terms", "Software development life cycle concepts");
                Deck sqlDeck = new Deck("SQL Basics", "Intro database review");

                deckRepository.saveAll(List.of(javaDeck, sdlcDeck, sqlDeck));

                cardRepository.saveAll(List.of(
                        new Card(javaDeck, "What is a class?", "A blueprint for creating objects.", 1),
                        new Card(javaDeck, "What is inheritance?", "A way for one class to derive from another.", 2),

                        new Card(sdlcDeck, "What does SDLC stand for?", "Software Development Life Cycle.", 1),
                        new Card(sdlcDeck, "What is Agile?", "An iterative development approach.", 2),

                        new Card(sqlDeck, "What does SQL stand for?", "Structured Query Language.", 1),
                        new Card(sqlDeck, "What is a primary key?", "A column that uniquely identifies each row.", 2)
                ));
            }
        };
    }
}
