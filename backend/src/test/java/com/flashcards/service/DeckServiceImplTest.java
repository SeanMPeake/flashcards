package com.flashcards.service;

import com.flashcards.dto.request.CreateDeckRequest;
import com.flashcards.dto.request.UpdateDeckRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

// Service Unit Test — tests DeckServiceImpl logic with repository dependencies mocked via Mockito.
// No Spring context is loaded; only the service class itself is exercised.
@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

    @Test
    @DisplayName("getDecks returns deck summaries")
    void getDecksReturnsDeckSummaries() {
        List<DeckSummaryResponse> summaries = List.of(
                new DeckSummaryResponse(1L, "Java Basics", "Core Java review cards", 2),
                new DeckSummaryResponse(2L, "SQL Basics", "Intro database review", 0)
        );

        given(deckRepository.findDeckSummaries()).willReturn(summaries);

        List<DeckSummaryResponse> result = deckService.getDecks();

        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getName());
        assertEquals("Core Java review cards", result.get(0).getDescription());
        assertEquals(2, result.get(0).getCardCount());
        assertEquals("SQL Basics", result.get(1).getName());
        assertEquals("Intro database review", result.get(1).getDescription());
        assertEquals(0, result.get(1).getCardCount());
    }

    @Test
    @DisplayName("getDecks returns empty list when repository is empty")
    void getDecksReturnsEmptyListWhenRepositoryEmpty() {
        given(deckRepository.findDeckSummaries()).willReturn(List.of());

        List<DeckSummaryResponse> result = deckService.getDecks();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getDeckById returns deck with mapped cards")
    void getDeckByIdReturnsDeckWithMappedCards() throws Exception {
        Deck deck = new Deck("SQL Basics", "Intro database review");
        setId(deck, 1L);

        Card card = createCard(10L, deck, "What does SQL stand for?", "Structured Query Language.", 1);
        deck.getCards().add(card);

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
        Deck deck = new Deck("Empty Deck", "No cards yet");
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

    @Test
    @DisplayName("createDeck saves and returns mapped response")
    void createDeckSavesAndReturnsResponse() throws Exception {
        CreateDeckRequest request = new CreateDeckRequest();
        request.setName("New Deck");
        request.setDescription("A new deck");

        Deck saved = new Deck("New Deck", "A new deck");
        setId(saved, 3L);

        given(deckRepository.save(any(Deck.class))).willReturn(saved);

        DeckResponse result = deckService.createDeck(request);

        assertEquals(3L, result.getId());
        assertEquals("New Deck", result.getName());
        assertEquals("A new deck", result.getDescription());
        assertEquals(0, result.getCardCount());
    }

    @Test
    @DisplayName("updateDeck updates fields and returns mapped response")
    void updateDeckUpdatesFieldsAndReturnsResponse() throws Exception {
        Deck deck = new Deck("Old Name", "Old description");
        setId(deck, 1L);

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName("New Name");
        request.setDescription("New description");

        given(deckRepository.findById(1L)).willReturn(Optional.of(deck));
        given(deckRepository.save(deck)).willReturn(deck);

        DeckResponse result = deckService.updateDeck(1L, request);

        assertEquals("New Name", result.getName());
        assertEquals("New description", result.getDescription());
    }

    @Test
    @DisplayName("updateDeck throws when deck is missing")
    void updateDeckThrowsWhenDeckMissing() {
        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName("Name");
        request.setDescription("Description");

        given(deckRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.updateDeck(999L, request));
    }

    @Test
    @DisplayName("deleteDeck removes deck when found")
    void deleteDeckRemovesDeck() {
        given(deckRepository.existsById(1L)).willReturn(true);

        deckService.deleteDeck(1L);

        then(deckRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteDeck throws when deck is missing")
    void deleteDeckThrowsWhenDeckMissing() {
        given(deckRepository.existsById(999L)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> deckService.deleteDeck(999L));
    }

    private Card createCard(Long id, Deck deck, String frontText, String backText, Integer priority) throws Exception {
        Card card = new Card(deck, frontText, backText, priority);
        setId(card, id);
        return card;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
