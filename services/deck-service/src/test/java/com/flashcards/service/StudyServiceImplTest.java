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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private StudyServiceImpl studyService;

    private SavePrioritiesRequest request(PriorityUpdate... updates) {
        return new SavePrioritiesRequest(List.of(updates));
    }

    @SuppressWarnings("unchecked")
    private List<Card> captureSaved() {
        ArgumentCaptor<List<Card>> captor = ArgumentCaptor.forClass(List.class);
        then(cardRepository).should().saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("savePriorities updates the priorities of cards in the user's deck")
    void savesPrioritiesForCardsInDeck() throws Exception {
        Card first = card(1L, 1);
        Card second = card(2L, 2);
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(cardRepository.findByDeckId(DECK)).willReturn(List.of(first, second));

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(1L, 13), new PriorityUpdate(2L, 8)));

        assertEquals(13, first.getPriority());
        assertEquals(8, second.getPriority());
        assertEquals(2, captureSaved().size());
    }

    @Test
    @DisplayName("savePriorities treats another user's deck as not found and changes nothing")
    void rejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class,
                () -> studyService.savePriorities(OTHER_USER, DECK, request(new PriorityUpdate(1L, 99))));
        then(cardRepository).should(never()).findByDeckId(any());
        then(cardRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("savePriorities skips card ids that aren't in the deck instead of failing")
    void skipsCardsNotInDeck() throws Exception {
        // Card 7 was deleted mid-study, or belongs to some other deck.
        Card inDeck = card(1L, 1);
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(cardRepository.findByDeckId(DECK)).willReturn(List.of(inDeck));

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(1L, 13), new PriorityUpdate(7L, 50)));

        assertEquals(13, inDeck.getPriority());
        List<Card> saved = captureSaved();
        assertEquals(1, saved.size());
        assertSame(inDeck, saved.get(0));
    }

    // A card with the given id and priority, belonging to the user's deck.
    private Card card(Long id, int priority) throws Exception {
        Deck deck = new Deck(USER, "Test", null);
        setId(deck, DECK);
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
