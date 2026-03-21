package com.flashcards.config;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataLoader {

    // Seeds starter data for development and testing when the application starts.
    @Bean
    CommandLineRunner loadData(DeckRepository deckRepository, CardRepository cardRepository) {
        return args -> {
            // Only seed data if the database is empty so the same records are not duplicated
            // each time the application restarts.
            if (deckRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                Deck javaDeck = new Deck("Java Basics", "Core Java review cards", now, now);
                Deck sdlcDeck = new Deck("SDLC Terms", "Software development life cycle concepts", now, now);
                Deck sqlDeck = new Deck("SQL Basics", "Intro database review", now, now);

                deckRepository.saveAll(List.of(javaDeck, sdlcDeck, sqlDeck));

                cardRepository.saveAll(List.of(
                        new Card(javaDeck, "What is a class?", "A blueprint for creating objects.", 1, now, now),
                        new Card(javaDeck, "What is inheritance?", "A way for one class to derive from another.", 2, now, now),

                        new Card(sdlcDeck, "What does SDLC stand for?", "Software Development Life Cycle.", 1, now, now),
                        new Card(sdlcDeck, "What is Agile?", "An iterative development approach.", 2, now, now),

                        new Card(sqlDeck, "What does SQL stand for?", "Structured Query Language.", 1, now, now),
                        new Card(sqlDeck, "What is a primary key?", "A column that uniquely identifies each row.", 2, now, now)
                ));
            }
        };
    }
}
