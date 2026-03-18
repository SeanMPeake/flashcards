package com.flashcards.service;

import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

    @Test
    @DisplayName("getDecks returns deck summaries")
    void getDecksReturnsDeckSummaries() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Deck deck1 = new Deck("Java Basics", "Core Java review cards", now, now);
        deck1.setCards(List.of(
                createCard(1L, deck1, "What is a class?", "A blueprint for creating objects.", 1, now, now),
                createCard(2L, deck1, "What is inheritance?", "A way for one class to derive from another.", 2, now, now)
        ));
        setId(deck1, 1L);

        Deck deck2 = new Deck("SQL Basics", "Intro database review", now, now);
        deck2.setCards(List.of());
        setId(deck2, 2L);

        given(deckRepository.findAll()).willReturn(List.of(deck1, deck2));

        List<DeckSummaryResponse> result = deckService.getDecks();

        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getName());
        assertEquals(2, result.get(0).getCardCount());
        assertEquals("SQL Basics", result.get(1).getName());
        assertEquals(0, result.get(1).getCardCount());
    }

    @Test
    @DisplayName("getDecks returns empty list when repository is empty")
    void getDecksReturnsEmptyListWhenRepositoryEmpty() {
        given(deckRepository.findAll()).willReturn(List.of());

        List<DeckSummaryResponse> result = deckService.getDecks();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getDeckById returns deck with mapped cards")
    void getDeckByIdReturnsDeckWithMappedCards() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Deck deck = new Deck("SQL Basics", "Intro database review", now, now);
        setId(deck, 1L);

        Card card = createCard(10L, deck, "What does SQL stand for?", "Structured Query Language.", 1, now, now);
        deck.setCards(List.of(card));

        given(deckRepository.findById(1L)).willReturn(Optional.of(deck));

        DeckResponse result = deckService.getDeckById(1L);

        assertEquals(1L, result.getId());
        assertEquals("SQL Basics", result.getName());
        assertEquals(1, result.getCardCount());
        assertNotNull(result.getCards());
        assertEquals(1, result.getCards().size());
        assertEquals("What does SQL stand for?", result.getCards().get(0).getFrontText());
        assertEquals("Structured Query Language.", result.getCards().get(0).getBackText());
        assertEquals(1, result.getCards().get(0).getPriority());
    }

    @Test
    @DisplayName("getDeckById returns empty cards when deck has no cards")
    void getDeckByIdReturnsEmptyCardsWhenDeckHasNoCards() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Deck deck = new Deck("Empty Deck", "No cards yet", now, now);
        deck.setCards(List.of());
        setId(deck, 5L);

        given(deckRepository.findById(5L)).willReturn(Optional.of(deck));

        DeckResponse result = deckService.getDeckById(5L);

        assertEquals(0, result.getCardCount());
        assertNotNull(result.getCards());
        assertTrue(result.getCards().isEmpty());
    }

    @Test
    @DisplayName("getDeckById throws when deck is missing")
    void getDeckByIdThrowsWhenDeckMissing() {
        given(deckRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.getDeckById(999L));
    }

    private Card createCard(Long id, Deck deck, String frontText, String backText, Integer priority,
                            LocalDateTime createdAt, LocalDateTime updatedAt) throws Exception {
        Card card = new Card(deck, frontText, backText, priority, createdAt, updatedAt);
        setId(card, id);
        return card;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
