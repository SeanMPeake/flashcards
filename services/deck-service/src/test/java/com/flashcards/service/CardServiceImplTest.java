package com.flashcards.service;

import com.flashcards.dto.request.CreateCardRequest;
import com.flashcards.dto.request.UpdateCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.exception.CardNotFoundException;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

// Service Unit Test — tests CardServiceImpl logic with repository dependencies mocked via Mockito.
// No Spring context is loaded; only the service class itself is exercised.
//
// Cards are owned through their deck, so every operation first checks that the
// deck belongs to the logged-in user.
@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    @DisplayName("createCard saves and returns mapped response")
    void createCardSavesAndReturnsResponse() throws Exception {
        Deck deck = new Deck(USER, "Java Basics", "Core Java review cards");
        setId(deck, 1L);

        Card saved = new Card(deck, "What is JVM?", "Java Virtual Machine.", 0);
        setId(saved, 5L);

        given(deckRepository.findByIdAndOwnerUserId(1L, USER)).willReturn(Optional.of(deck));
        given(cardRepository.save(any(Card.class))).willReturn(saved);

        CreateCardRequest request = new CreateCardRequest();
        request.setFrontText("What is JVM?");
        request.setBackText("Java Virtual Machine.");

        CardResponse result = cardService.createCard(USER, 1L, request);

        assertEquals(5L, result.getId());
        assertEquals("What is JVM?", result.getFrontText());
        assertEquals("Java Virtual Machine.", result.getBackText());
        assertEquals(0, result.getPriority());
    }

    @Test
    @DisplayName("createCard throws when the deck is not found")
    void createCardThrowsWhenDeckNotFound() {
        given(deckRepository.findByIdAndOwnerUserId(999L, USER)).willReturn(Optional.empty());

        CreateCardRequest request = new CreateCardRequest();
        request.setFrontText("Q");
        request.setBackText("A");

        assertThrows(DeckNotFoundException.class, () -> cardService.createCard(USER, 999L, request));
    }

    @Test
    @DisplayName("createCard throws for another user's deck and saves nothing")
    void createCardThrowsForAnotherUsersDeck() {
        given(deckRepository.findByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(Optional.empty());

        CreateCardRequest request = new CreateCardRequest();
        request.setFrontText("Q");
        request.setBackText("A");

        assertThrows(DeckNotFoundException.class, () -> cardService.createCard(OTHER_USER, 1L, request));
        then(cardRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("updateCard updates fields and returns mapped response")
    void updateCardUpdatesAndReturnsResponse() throws Exception {
        Deck deck = new Deck(USER, "Java Basics", "Core Java review cards");
        setId(deck, 1L);

        Card card = new Card(deck, "Old front", "Old back", 0);
        setId(card, 2L);

        given(deckRepository.existsByIdAndOwnerUserId(1L, USER)).willReturn(true);
        given(cardRepository.findById(2L)).willReturn(Optional.of(card));
        given(cardRepository.save(card)).willReturn(card);

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFrontText("New front");
        request.setBackText("New back");

        CardResponse result = cardService.updateCard(USER, 1L, 2L, request);

        assertEquals("New front", result.getFrontText());
        assertEquals("New back", result.getBackText());
    }

    @Test
    @DisplayName("updateCard throws when the deck is not found")
    void updateCardThrowsWhenDeckNotFound() {
        given(deckRepository.existsByIdAndOwnerUserId(999L, USER)).willReturn(false);

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFrontText("Q");
        request.setBackText("A");

        assertThrows(DeckNotFoundException.class, () -> cardService.updateCard(USER, 999L, 1L, request));
    }

    @Test
    @DisplayName("updateCard throws for a card in another user's deck and never loads it")
    void updateCardThrowsForAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(false);

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFrontText("Q");
        request.setBackText("A");

        assertThrows(DeckNotFoundException.class, () -> cardService.updateCard(OTHER_USER, 1L, 2L, request));
        then(cardRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("updateCard throws when the card is not found")
    void updateCardThrowsWhenCardNotFound() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, USER)).willReturn(true);
        given(cardRepository.findById(999L)).willReturn(Optional.empty());

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFrontText("Q");
        request.setBackText("A");

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(USER, 1L, 999L, request));
    }

    @Test
    @DisplayName("updateCard throws when the card does not belong to the specified deck")
    void updateCardThrowsWhenCardNotInDeck() throws Exception {
        Deck deckOne = new Deck(USER, "Deck One", "First deck");
        setId(deckOne, 1L);

        Card card = new Card(deckOne, "Q", "A", 0);
        setId(card, 5L);

        // The user owns deck 2 as well, but card 5 lives in deck 1.
        given(deckRepository.existsByIdAndOwnerUserId(2L, USER)).willReturn(true);
        given(cardRepository.findById(5L)).willReturn(Optional.of(card));

        UpdateCardRequest request = new UpdateCardRequest();
        request.setFrontText("New Q");
        request.setBackText("New A");

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(USER, 2L, 5L, request));
    }

    @Test
    @DisplayName("deleteCard removes the card when found")
    void deleteCardRemovesCard() throws Exception {
        Deck deck = new Deck(USER, "Java Basics", "Core Java review cards");
        setId(deck, 1L);

        Card card = new Card(deck, "Q", "A", 0);
        setId(card, 2L);

        given(deckRepository.existsByIdAndOwnerUserId(1L, USER)).willReturn(true);
        given(cardRepository.findById(2L)).willReturn(Optional.of(card));

        cardService.deleteCard(USER, 1L, 2L);

        then(cardRepository).should().deleteById(2L);
    }

    @Test
    @DisplayName("deleteCard throws for a card in another user's deck and deletes nothing")
    void deleteCardThrowsForAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> cardService.deleteCard(OTHER_USER, 1L, 2L));
        then(cardRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteCard throws when the card is not found")
    void deleteCardThrowsWhenCardNotFound() {
        given(deckRepository.existsByIdAndOwnerUserId(1L, USER)).willReturn(true);
        given(cardRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(USER, 1L, 999L));
    }

    @Test
    @DisplayName("deleteCard throws when the card does not belong to the specified deck")
    void deleteCardThrowsWhenCardNotInDeck() throws Exception {
        Deck deckOne = new Deck(USER, "Deck One", "First deck");
        setId(deckOne, 1L);

        Card card = new Card(deckOne, "Q", "A", 0);
        setId(card, 5L);

        given(deckRepository.existsByIdAndOwnerUserId(2L, USER)).willReturn(true);
        given(cardRepository.findById(5L)).willReturn(Optional.of(card));

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(USER, 2L, 5L));
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
