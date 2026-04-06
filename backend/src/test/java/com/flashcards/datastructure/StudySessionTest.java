package com.flashcards.datastructure;

import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit Test — verifies StudySession scheduling logic in isolation with no Spring context or mocked dependencies.
class StudySessionTest {

    private Card makeCard(Long id, String frontText, int priority) throws Exception {
        Deck deck = new Deck("Test", null);
        Card card = new Card(deck, frontText, "back", priority);
        setId(card, id);
        return card;
    }

    @Test
    @DisplayName("nextNode returns card with the lowest priority first")
    void nextNodeReturnsMostUrgentCard() throws Exception {
        Card c1 = makeCard(1L, "Card 1", 3);
        Card c2 = makeCard(2L, "Card 2", 1);
        Card c3 = makeCard(3L, "Card 3", 2);
        StudySession session = new StudySession(List.of(c1, c2, c3));

        MinHeap.HeapNode node = session.nextNode();

        assertEquals(1, node.priority);
        assertEquals(2L, node.cardId);
    }

    @Test
    @DisplayName("normal reschedule sets new priority to oldPriority + deckSize")
    void rescheduleNormalViewAddsFullDeckSize() throws Exception {
        Card c1 = makeCard(1L, "Card 1", 1);
        Card c2 = makeCard(2L, "Card 2", 2);
        StudySession session = new StudySession(List.of(c1, c2));

        session.nextNode(); // remove card 1 from heap (priority 1)
        int newPriority = session.reschedule(1L, 1, false);

        // deckSize = 2, newPriority = 1 + 2 = 3
        assertEquals(3, newPriority);
    }

    @Test
    @DisplayName("mark for review reschedule sets new priority to oldPriority + deckSize / 2")
    void rescheduleMarkForReviewAddsHalfDeckSize() throws Exception {
        Card c1 = makeCard(1L, "Card 1", 1);
        Card c2 = makeCard(2L, "Card 2", 2);
        Card c3 = makeCard(3L, "Card 3", 3);
        Card c4 = makeCard(4L, "Card 4", 4);
        StudySession session = new StudySession(List.of(c1, c2, c3, c4));

        session.nextNode(); // remove card 1 (priority 1)
        int newPriority = session.reschedule(1L, 1, true);

        // deckSize = 4, newPriority = 1 + (4 / 2) = 3
        assertEquals(3, newPriority);
    }

    @Test
    @DisplayName("findCard returns the correct card for a given ID")
    void findCardReturnsCorrectCard() throws Exception {
        Card c1 = makeCard(1L, "What is JVM?", 1);
        Card c2 = makeCard(2L, "What is JRE?", 2);
        StudySession session = new StudySession(List.of(c1, c2));

        Card found = session.findCard(2L);

        assertNotNull(found);
        assertEquals("What is JRE?", found.getFrontText());
    }

    @Test
    @DisplayName("mark for review places card between early and late cards in the deck")
    void markForReviewPositionsCardInMiddleOfDeck() throws Exception {
        // deckSize = 4, deckSize / 2 = 2
        // Card 1 at priority 1 marked for review → newPriority = 1 + 2 = 3
        // Should come after card 2 (priority 2) but before card 3 (priority 5) and card 4 (priority 10)
        Card c1 = makeCard(1L, "Card 1", 1);
        Card c2 = makeCard(2L, "Card 2", 2);
        Card c3 = makeCard(3L, "Card 3", 5);
        Card c4 = makeCard(4L, "Card 4", 10);
        StudySession session = new StudySession(List.of(c1, c2, c3, c4));

        MinHeap.HeapNode first = session.nextNode();
        session.reschedule(first.cardId, first.priority, true);

        assertEquals(2L, session.nextNode().cardId); // card 2 comes before the rescheduled card
        MinHeap.HeapNode rescheduled = session.nextNode();
        assertEquals(1L, rescheduled.cardId);        // card 1 comes back here, not at the end
        assertEquals(3, rescheduled.priority);
    }

    @Test
    @DisplayName("reschedule reinserts card so the other card comes next")
    void reschedulePositionsCardBehindOtherCards() throws Exception {
        Card c1 = makeCard(1L, "Card 1", 1);
        Card c2 = makeCard(2L, "Card 2", 2);
        StudySession session = new StudySession(List.of(c1, c2));

        // Remove card 1 (priority 1) and reschedule it normally: newPriority = 1 + 2 = 3
        MinHeap.HeapNode first = session.nextNode();
        session.reschedule(first.cardId, first.priority, false);

        // Card 2 (priority 2) should come before the rescheduled card 1 (priority 3)
        MinHeap.HeapNode next = session.nextNode();
        assertEquals(2L, next.cardId);
        assertEquals(2, next.priority);
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
