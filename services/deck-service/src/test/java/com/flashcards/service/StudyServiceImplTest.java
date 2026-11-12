package com.flashcards.service;

import com.flashcards.datastructure.MinHeap;
import com.flashcards.datastructure.StudySession;
import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;
import com.flashcards.exception.CardNotFoundException;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EmptyDeckException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

// Service Unit Test — tests StudyServiceImpl logic with all dependencies mocked via Mockito.
// No Spring context is loaded; only the service class itself is exercised.
//
// Study works only on the logged-in user's decks, and sessions are kept per user per deck.
@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long DECK = 1L;

    @Mock
    private StudySessionManager sessionManager;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private StudyServiceImpl studyService;

    @Test
    @DisplayName("startSession returns the first card of the user's deck")
    void startSessionReturnsFirstCard() throws Exception {
        Card card = makeCard(1L, DECK, "What is JVM?", "Java Virtual Machine.", 1);
        List<Card> cards = List.of(card);

        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode node = new MinHeap.HeapNode(1, 1L);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(cardRepository.findByDeckId(DECK)).willReturn(cards);
        given(sessionManager.createSession(USER, DECK, cards)).willReturn(session);
        given(session.nextNode()).willReturn(node);
        given(session.findCard(1L)).willReturn(card);
        given(session.getDeckSize()).willReturn(1);

        StudyStartResponse result = studyService.startSession(USER, DECK);

        assertEquals(1L, result.getCard().getId());
        assertEquals("What is JVM?", result.getCard().getFrontText());
        assertEquals(1, result.getCard().getPriority());
        assertEquals(1, result.getTotalCards());
    }

    @Test
    @DisplayName("startSession throws DeckNotFoundException when the deck does not exist")
    void startSessionThrowsWhenDeckNotFound() {
        given(deckRepository.existsByIdAndOwnerUserId(999L, USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> studyService.startSession(USER, 999L));
    }

    @Test
    @DisplayName("startSession treats another user's deck as not found and builds no session")
    void startSessionRejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> studyService.startSession(OTHER_USER, DECK));
        then(sessionManager).should(never()).createSession(any(), any(), any());
    }

    @Test
    @DisplayName("startSession throws EmptyDeckException when the deck has no cards")
    void startSessionThrowsWhenDeckIsEmpty() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(cardRepository.findByDeckId(DECK)).willReturn(List.of());

        assertThrows(EmptyDeckException.class, () -> studyService.startSession(USER, DECK));
    }

    @Test
    @DisplayName("nextCard reschedules the viewed card and returns the next card")
    void nextCardReschedulesAndReturnsNextCard() throws Exception {
        Card viewed = makeCard(1L, DECK, "What is JVM?", "Java Virtual Machine.", 1);
        Card next = makeCard(2L, DECK, "What is JRE?", "Java Runtime Environment.", 2);

        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode nextNode = new MinHeap.HeapNode(2, 2L);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(session);
        given(cardRepository.findById(1L)).willReturn(Optional.of(viewed));
        given(session.reschedule(1L, 1, false)).willReturn(31);
        given(cardRepository.save(viewed)).willReturn(viewed);
        given(session.nextNode()).willReturn(nextNode);
        given(session.findCard(2L)).willReturn(next);

        CardResponse result = studyService.nextCard(USER, DECK, request(1L));

        assertEquals(2L, result.getId());
        assertEquals("What is JRE?", result.getFrontText());
    }

    @Test
    @DisplayName("nextCard rejects another user's deck even when a session id matches")
    void nextCardRejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> studyService.nextCard(OTHER_USER, DECK, request(1L)));
        then(sessionManager).should(never()).getSession(any(), any());
        then(cardRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("nextCard rejects a card from a different deck and changes nothing")
    void nextCardRejectsCardFromAnotherDeck() throws Exception {
        // Card 5 lives in deck 2 (possibly someone else's), not the deck being studied.
        Card foreign = makeCard(5L, 2L, "Q", "A", 1);
        StudySession session = mock(StudySession.class);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(session);
        given(cardRepository.findById(5L)).willReturn(Optional.of(foreign));

        assertThrows(CardNotFoundException.class, () -> studyService.nextCard(USER, DECK, request(5L)));
        then(session).should(never()).reschedule(anyLong(), anyInt(), anyBoolean());
        then(cardRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("nextCard rebuilds the session when none exists")
    void nextCardRebuildsSessionWhenMissing() throws Exception {
        Card card = makeCard(1L, DECK, "What is JVM?", "Java Virtual Machine.", 1);
        List<Card> cards = List.of(card);

        StudySession rebuilt = mock(StudySession.class);
        MinHeap.HeapNode node = new MinHeap.HeapNode(31, 1L);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(null);
        given(cardRepository.findByDeckId(DECK)).willReturn(cards);
        given(sessionManager.createSession(eq(USER), eq(DECK), any())).willReturn(rebuilt);
        given(cardRepository.findById(1L)).willReturn(Optional.of(card));
        given(rebuilt.reschedule(1L, 1, false)).willReturn(31);
        given(cardRepository.save(card)).willReturn(card);
        given(rebuilt.nextNode()).willReturn(node);
        given(rebuilt.findCard(1L)).willReturn(card);

        CardResponse result = studyService.nextCard(USER, DECK, request(1L));

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("nextCard throws EmptyDeckException when the session is missing and the deck has no cards")
    void nextCardThrowsWhenSessionMissingAndDeckEmpty() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(null);
        given(cardRepository.findByDeckId(DECK)).willReturn(List.of());

        assertThrows(EmptyDeckException.class, () -> studyService.nextCard(USER, DECK, request(1L)));
    }

    @Test
    @DisplayName("nextCard persists the updated priority for the rescheduled card")
    void nextCardPersistsUpdatedPriority() throws Exception {
        Card viewed = makeCard(1L, DECK, "What is JVM?", "Java Virtual Machine.", 1);
        Card next = makeCard(2L, DECK, "What is JRE?", "Java Runtime Environment.", 2);

        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode nextNode = new MinHeap.HeapNode(2, 2L);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(session);
        given(cardRepository.findById(1L)).willReturn(Optional.of(viewed));
        given(session.reschedule(1L, 1, false)).willReturn(13);
        given(cardRepository.save(viewed)).willReturn(viewed);
        given(session.nextNode()).willReturn(nextNode);
        given(session.findCard(2L)).willReturn(next);

        studyService.nextCard(USER, DECK, request(1L));

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        then(cardRepository).should().save(captor.capture());
        assertEquals(13, captor.getValue().getPriority());
    }

    @Test
    @DisplayName("nextCard throws IllegalStateException when the next card is missing from the session map")
    void nextCardThrowsWhenCardMissingFromSessionMap() throws Exception {
        Card viewed = makeCard(1L, DECK, "Q", "A", 1);
        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode node = new MinHeap.HeapNode(1, 99L);

        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        given(sessionManager.getSession(USER, DECK)).willReturn(session);
        given(cardRepository.findById(1L)).willReturn(Optional.of(viewed));
        given(session.reschedule(1L, 1, false)).willReturn(31);
        given(cardRepository.save(any())).willReturn(viewed);
        given(session.nextNode()).willReturn(node);
        given(session.findCard(99L)).willReturn(null);

        assertThrows(IllegalStateException.class, () -> studyService.nextCard(USER, DECK, request(1L)));
    }

    private NextCardRequest request(Long cardId) {
        NextCardRequest request = new NextCardRequest();
        request.setCardId(cardId);
        request.setPriority(1);
        request.setMarkForReview(false);
        return request;
    }

    // A card belonging to the deck with the given id.
    private Card makeCard(Long id, Long deckId, String frontText, String backText, int priority) throws Exception {
        Deck deck = new Deck(USER, "Test", null);
        setId(deck, deckId);
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
