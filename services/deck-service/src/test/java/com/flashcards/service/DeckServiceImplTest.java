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
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;

// Service Unit Test — tests DeckServiceImpl logic with repository dependencies mocked via Mockito.
// No Spring context is loaded; only the service class itself is exercised.
//
// The repository mocks stand in for the owner-scoped queries: a deck that belongs to
// someone else comes back empty, exactly as the real query returns it.
@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

    @Test
    @DisplayName("getDecks returns only the user's deck summaries")
    void getDecksReturnsDeckSummaries() {
        List<DeckSummaryResponse> summaries = List.of(
                new DeckSummaryResponse(1L, "Java Basics", "Core Java review cards", 2),
                new DeckSummaryResponse(2L, "SQL Basics", "Intro database review", 0)
        );

        given(deckRepository.findDeckSummaries(USER)).willReturn(summaries);

        List<DeckSummaryResponse> result = deckService.getDecks(USER);

        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getName());
        assertEquals("Core Java review cards", result.get(0).getDescription());
        assertEquals(2, result.get(0).getCardCount());
        assertEquals("SQL Basics", result.get(1).getName());
        assertEquals("Intro database review", result.get(1).getDescription());
        assertEquals(0, result.get(1).getCardCount());
        then(deckRepository).should().findDeckSummaries(USER);
    }

    @Test
    @DisplayName("getDecks returns an empty list for a user with no decks")
    void getDecksReturnsEmptyListWhenUserHasNoDecks() {
        given(deckRepository.findDeckSummaries(USER)).willReturn(List.of());

        List<DeckSummaryResponse> result = deckService.getDecks(USER);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getDeckById returns the user's deck with mapped cards")
    void getDeckByIdReturnsDeckWithMappedCards() throws Exception {
        Deck deck = new Deck(USER, "SQL Basics", "Intro database review");
        setId(deck, 1L);

        Card card = createCard(10L, deck, "What does SQL stand for?", "Structured Query Language.", 1);
        deck.getCards().add(card);

        given(deckRepository.findByIdAndOwnerUserId(1L, USER)).willReturn(Optional.of(deck));

        DeckResponse result = deckService.getDeckById(USER, 1L);

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
        Deck deck = new Deck(USER, "Empty Deck", "No cards yet");
        setId(deck, 5L);

        given(deckRepository.findByIdAndOwnerUserId(5L, USER)).willReturn(Optional.of(deck));

        DeckResponse result = deckService.getDeckById(USER, 5L);

        assertEquals(0, result.getCardCount());
        assertNotNull(result.getCards());
        assertTrue(result.getCards().isEmpty());
    }

    @Test
    @DisplayName("getDeckById throws when deck is missing")
    void getDeckByIdThrowsWhenDeckMissing() {
        given(deckRepository.findByIdAndOwnerUserId(999L, USER)).willReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.getDeckById(USER, 999L));
    }

    @Test
    @DisplayName("getDeckById treats another user's deck as not found and never uses an unscoped lookup")
    void getDeckByIdHidesAnotherUsersDeck() {
        // Deck 1 belongs to USER; OTHER_USER's scoped lookup finds nothing.
        given(deckRepository.findByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.getDeckById(OTHER_USER, 1L));
        then(deckRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("createDeck records the logged-in user as the owner")
    void createDeckSavesWithOwnerAndReturnsResponse() throws Exception {
        CreateDeckRequest request = new CreateDeckRequest();
        request.setName("New Deck");
        request.setDescription("A new deck");

        Deck saved = new Deck(USER, "New Deck", "A new deck");
        setId(saved, 3L);

        given(deckRepository.save(any(Deck.class))).willReturn(saved);

        DeckResponse result = deckService.createDeck(USER, request);

        ArgumentCaptor<Deck> captor = ArgumentCaptor.forClass(Deck.class);
        then(deckRepository).should().save(captor.capture());
        assertEquals(USER, captor.getValue().getOwnerUserId());

        assertEquals(3L, result.getId());
        assertEquals("New Deck", result.getName());
        assertEquals("A new deck", result.getDescription());
        assertEquals(0, result.getCardCount());
    }

    @Test
    @DisplayName("updateDeck updates fields and returns mapped response")
    void updateDeckUpdatesFieldsAndReturnsResponse() throws Exception {
        Deck deck = new Deck(USER, "Old Name", "Old description");
        setId(deck, 1L);

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName("New Name");
        request.setDescription("New description");

        given(deckRepository.findByIdAndOwnerUserId(1L, USER)).willReturn(Optional.of(deck));
        given(deckRepository.save(deck)).willReturn(deck);

        DeckResponse result = deckService.updateDeck(USER, 1L, request);

        assertEquals("New Name", result.getName());
        assertEquals("New description", result.getDescription());
    }

    @Test
    @DisplayName("updateDeck throws for another user's deck and saves nothing")
    void updateDeckThrowsForAnotherUsersDeck() {
        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setName("Name");
        request.setDescription("Description");

        given(deckRepository.findByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.updateDeck(OTHER_USER, 1L, request));
        then(deckRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("deleteDeck removes the user's deck")
    void deleteDeckRemovesDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, USER)).willReturn(true);

        deckService.deleteDeck(USER, 1L);

        then(deckRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteDeck throws for another user's deck and deletes nothing")
    void deleteDeckThrowsForAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> deckService.deleteDeck(OTHER_USER, 1L));
        then(deckRepository).should(never()).deleteById(any());
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
