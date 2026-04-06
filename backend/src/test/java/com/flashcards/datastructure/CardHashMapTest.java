package com.flashcards.datastructure;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit Test — verifies CardHashMap logic in isolation with no Spring context or mocked dependencies.
class CardHashMapTest {

    private Card makeCard(Long id, String frontText, int priority) throws Exception {
        Deck deck = new Deck("Test", null);
        Card card = new Card(deck, frontText, "back", priority);
        setId(card, id);
        return card;
    }

    @Test
    @DisplayName("search returns card after insert")
    void searchReturnsInsertedCard() throws Exception {
        Card card = makeCard(1L, "What is JVM?", 1);
        CardHashMap map = new CardHashMap(List.of(card));

        Card result = map.search(1L);

        assertNotNull(result);
        assertEquals("What is JVM?", result.getFrontText());
    }

    @Test
    @DisplayName("search returns null when card ID is not in map")
    void searchReturnsNullWhenNotFound() throws Exception {
        Card card = makeCard(1L, "What is JVM?", 1);
        CardHashMap map = new CardHashMap(List.of(card));

        assertNull(map.search(99L));
    }

    @Test
    @DisplayName("search finds all cards in a multi-card map")
    void searchFindsAllCardsInMap() throws Exception {
        Card card1 = makeCard(1L, "Question 1", 1);
        Card card2 = makeCard(2L, "Question 2", 2);
        Card card3 = makeCard(3L, "Question 3", 3);
        CardHashMap map = new CardHashMap(List.of(card1, card2, card3));

        assertEquals("Question 1", map.search(1L).getFrontText());
        assertEquals("Question 2", map.search(2L).getFrontText());
        assertEquals("Question 3", map.search(3L).getFrontText());
    }

    @Test
    @DisplayName("separate chaining resolves collision between IDs that hash to the same bucket")
    void separateChainingHandlesCollision() throws Exception {
        // Building from 1 card gives tableSize = nextPrime(2) = 2.
        // hash(1L) = abs(1 * 31) % 2 = 1
        // hash(3L) = abs(3 * 31) % 2 = 1  <- same bucket, forces a chain
        Card card1 = makeCard(1L, "Card One", 1);
        CardHashMap map = new CardHashMap(List.of(card1));

        Card card3 = makeCard(3L, "Card Three", 3);
        map.insert(3L, card3);

        assertEquals("Card One", map.search(1L).getFrontText());
        assertEquals("Card Three", map.search(3L).getFrontText());
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
