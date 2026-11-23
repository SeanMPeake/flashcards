package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.dto.request.SavePrioritiesRequest.PriorityUpdate;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Card;
import com.flashcards.model.Deck;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

// Service Unit Test — StudyServiceImpl only saves priorities sent by the browser.
// Repositories are mocked; real Card objects are used so changed priorities can be checked.
@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long DECK = 1L;
    private static final Long OTHER_DECK = 2L;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private StudyServiceImpl studyService;

    private SavePrioritiesRequest request(PriorityUpdate... updates) {
        return new SavePrioritiesRequest(List.of(updates));
    }

    // The cards passed to saveAll.
    @SuppressWarnings("unchecked")
    private List<Card> captureSaved() {
        ArgumentCaptor<Iterable<Card>> captor = ArgumentCaptor.forClass(Iterable.class);
        then(cardRepository).should().saveAll(captor.capture());
        List<Card> saved = new ArrayList<>();
        captor.getValue().forEach(saved::add);
        return saved;
    }

    private void ownsDeckWithCards(Card... cards) {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(cardRepository.findAllById(anyIterable())).willReturn(List.of(cards));
    }

    @Test
    @DisplayName("savePriorities raises the priorities of cards in the user's deck")
    void savesHigherPriorities() throws Exception {
        Card first = card(1L, DECK, 1);
        Card second = card(2L, DECK, 2);
        ownsDeckWithCards(first, second);

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(1L, 13), new PriorityUpdate(2L, 8)));

        assertEquals(13, first.getPriority());
        assertEquals(8, second.getPriority());
        assertEquals(2, captureSaved().size());
    }

    @Test
    @DisplayName("savePriorities never lowers a priority (an older batch arriving late is ignored)")
    void ignoresLowerPriority() throws Exception {
        // Card 4 is already at 40 (newer save landed first); the late batch says 17.
        Card card = card(4L, DECK, 40);
        ownsDeckWithCards(card);

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(4L, 17)));

        assertEquals(40, card.getPriority());
        assertTrue(captureSaved().isEmpty());
    }

    @Test
    @DisplayName("A card listed twice in one batch keeps the highest value")
    void duplicateCardKeepsHighest() throws Exception {
        Card card = card(4L, DECK, 5);
        ownsDeckWithCards(card);

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(4L, 40), new PriorityUpdate(4L, 17)));

        assertEquals(40, card.getPriority());
        assertEquals(1, captureSaved().size());
    }

    @Test
    @DisplayName("savePriorities treats another user's deck as not found and changes nothing")
    void rejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class,
                () -> studyService.savePriorities(OTHER_USER, DECK, request(new PriorityUpdate(1L, 99))));
        then(cardRepository).should(never()).findAllById(any());
        then(cardRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("Card ids from another deck, or deleted ones, are skipped instead of failing")
    void skipsCardsNotInDeck() throws Exception {
        Card inDeck = card(1L, DECK, 1);
        Card elsewhere = card(7L, OTHER_DECK, 1);   // exists, but in a different deck
        ownsDeckWithCards(inDeck, elsewhere);       // card 9 doesn't exist at all

        studyService.savePriorities(USER, DECK, request(
                new PriorityUpdate(1L, 13), new PriorityUpdate(7L, 50), new PriorityUpdate(9L, 60)));

        assertEquals(13, inDeck.getPriority());
        assertEquals(1, elsewhere.getPriority());
        Collection<Card> saved = captureSaved();
        assertEquals(1, saved.size());
        assertTrue(saved.contains(inDeck));
    }

    // A card with the given id and priority, belonging to the given deck.
    private Card card(Long id, Long deckId, int priority) throws Exception {
        Deck deck = new Deck(USER, "Test", null);
        setId(deck, deckId);
        Card card = new Card(deck, "Q", "A", priority);
        setId(card, id);
        return card;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
