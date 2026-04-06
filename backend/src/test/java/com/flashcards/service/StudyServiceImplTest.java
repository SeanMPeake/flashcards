package com.flashcards.service;

import com.flashcards.datastructure.MinHeap;
import com.flashcards.datastructure.StudySession;
import com.flashcards.dto.request.NextCardRequest;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EmptyDeckException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

// Service Unit Test — tests StudyServiceImpl logic with all dependencies mocked via Mockito.
// No Spring context is loaded; only the service class itself is exercised.
@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    @Mock
    private StudySessionManager sessionManager;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private StudyServiceImpl studyService;

    @Test
    @DisplayName("startSession returns first card for valid deck")
    void startSessionReturnsFirstCard() throws Exception {
        Card card = makeCard(1L, "What is JVM?", "Java Virtual Machine.", 1);
        List<Card> cards = List.of(card);

        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode node = new MinHeap.HeapNode(1, 1L);

        given(deckRepository.existsById(1L)).willReturn(true);
        given(cardRepository.findByDeckId(1L)).willReturn(cards);
        given(sessionManager.createSession(1L, cards)).willReturn(session);
        given(session.nextNode()).willReturn(node);
        given(session.findCard(1L)).willReturn(card);

        CardResponse result = studyService.startSession(1L);

        assertEquals(1L, result.getId());
        assertEquals("What is JVM?", result.getFrontText());
        assertEquals(1, result.getPriority());
    }

    @Test
    @DisplayName("startSession throws DeckNotFoundException when deck does not exist")
    void startSessionThrowsWhenDeckNotFound() {
        given(deckRepository.existsById(999L)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> studyService.startSession(999L));
    }

    @Test
    @DisplayName("startSession throws EmptyDeckException when deck has no cards")
    void startSessionThrowsWhenDeckIsEmpty() {
        given(deckRepository.existsById(1L)).willReturn(true);
        given(cardRepository.findByDeckId(1L)).willReturn(List.of());

        assertThrows(EmptyDeckException.class, () -> studyService.startSession(1L));
    }

    @Test
    @DisplayName("nextCard reschedules viewed card and returns next card")
    void nextCardReschedulesAndReturnsNextCard() throws Exception {
        Card viewed = makeCard(1L, "What is JVM?", "Java Virtual Machine.", 1);
        Card next = makeCard(2L, "What is JRE?", "Java Runtime Environment.", 2);

        StudySession session = mock(StudySession.class);
        MinHeap.HeapNode nextNode = new MinHeap.HeapNode(2, 2L);

        NextCardRequest request = new NextCardRequest();
        request.setCardId(1L);
        request.setPriority(1);
        request.setMarkForReview(false);

        given(sessionManager.getSession(1L)).willReturn(session);
        given(session.reschedule(1L, 1, false)).willReturn(31);
        given(cardRepository.findById(1L)).willReturn(Optional.of(viewed));
        given(cardRepository.save(viewed)).willReturn(viewed);
        given(session.nextNode()).willReturn(nextNode);
        given(session.findCard(2L)).willReturn(next);

        CardResponse result = studyService.nextCard(1L, request);

        assertEquals(2L, result.getId());
        assertEquals("What is JRE?", result.getFrontText());
    }

    @Test
    @DisplayName("nextCard rebuilds session when no session exists")
    void nextCardRebuildsSessionWhenMissing() throws Exception {
        Card card = makeCard(1L, "What is JVM?", "Java Virtual Machine.", 1);
        List<Card> cards = List.of(card);

        StudySession rebuilt = mock(StudySession.class);
        MinHeap.HeapNode node = new MinHeap.HeapNode(31, 1L);

        NextCardRequest request = new NextCardRequest();
        request.setCardId(1L);
        request.setPriority(1);
        request.setMarkForReview(false);

        given(sessionManager.getSession(1L)).willReturn(null);
        given(deckRepository.existsById(1L)).willReturn(true);
        given(cardRepository.findByDeckId(1L)).willReturn(cards);
        given(sessionManager.createSession(eq(1L), any())).willReturn(rebuilt);
        given(rebuilt.reschedule(1L, 1, false)).willReturn(31);
        given(cardRepository.findById(1L)).willReturn(Optional.of(card));
        given(cardRepository.save(card)).willReturn(card);
        given(rebuilt.nextNode()).willReturn(node);
        given(rebuilt.findCard(1L)).willReturn(card);

        CardResponse result = studyService.nextCard(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("nextCard throws DeckNotFoundException when session is missing and deck does not exist")
    void nextCardThrowsWhenSessionMissingAndDeckNotFound() {
        NextCardRequest request = new NextCardRequest();
        request.setCardId(1L);
        request.setPriority(1);
        request.setMarkForReview(false);

        given(sessionManager.getSession(999L)).willReturn(null);
        given(deckRepository.existsById(999L)).willReturn(false);

        assertThrows(DeckNotFoundException.class, () -> studyService.nextCard(999L, request));
    }

    @Test
    @DisplayName("nextCard throws EmptyDeckException when session is missing and deck has no cards")
    void nextCardThrowsWhenSessionMissingAndDeckEmpty() {
        NextCardRequest request = new NextCardRequest();
        request.setCardId(1L);
        request.setPriority(1);
        request.setMarkForReview(false);

        given(sessionManager.getSession(1L)).willReturn(null);
        given(deckRepository.existsById(1L)).willReturn(true);
        given(cardRepository.findByDeckId(1L)).willReturn(List.of());

        assertThrows(EmptyDeckException.class, () -> studyService.nextCard(1L, request));
    }

    private Card makeCard(Long id, String frontText, String backText, int priority) throws Exception {
        Deck deck = new Deck("Test", null);
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
