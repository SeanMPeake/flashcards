package com.flashcards.service;

import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.dto.request.SavePrioritiesRequest.PriorityUpdate;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EventPublishException;
import com.flashcards.messaging.StudyEventPublisher;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

// Service Unit Test — StudyServiceImpl saves a batch of study progress, then publishes
// a StudyProgressSaved event.
//
// The never-lower, same-deck-only rule lives in one atomic database UPDATE
// (CardRepository.raisePriority); these tests check the service hands every entry to
// it for the right deck and owner. That the database itself rejects lower values,
// including under simultaneous saves, was checked live against Postgres.
@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;
    private static final Long DECK = 1L;
    private static final UUID BATCH = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SESSION = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private StudyEventPublisher eventPublisher;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private StudyServiceImpl studyService;

    // The mocked TransactionTemplate simply runs the work it is given.
    @BeforeEach
    @SuppressWarnings("unchecked")
    void runTransactionsDirectly() {
        lenient().doAnswer(inv -> {
            ((Consumer<TransactionStatus>) inv.getArgument(0)).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    private SavePrioritiesRequest request(int reviewed, int flagged, PriorityUpdate... updates) {
        return new SavePrioritiesRequest(BATCH, SESSION, reviewed, flagged, List.of(updates));
    }

    @Test
    @DisplayName("Saves each priority through the deck-scoped, raise-only update")
    void raisesEachPriorityWithinTheDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(2, 0, new PriorityUpdate(1L, 13), new PriorityUpdate(2L, 8)));

        then(cardRepository).should().raisePriority(eq(DECK), eq(1L), eq(13), any());
        then(cardRepository).should().raisePriority(eq(DECK), eq(2L), eq(8), any());
    }

    @Test
    @DisplayName("Publishes a StudyProgressSaved event describing the batch, after saving")
    void publishesEventAfterSaving() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(5, 2, new PriorityUpdate(1L, 13)));

        // The save happens first, the event second.
        InOrder order = inOrder(cardRepository, eventPublisher);
        order.verify(cardRepository).raisePriority(eq(DECK), eq(1L), eq(13), any());
        ArgumentCaptor<StudyProgressSaved> captor = ArgumentCaptor.forClass(StudyProgressSaved.class);
        order.verify(eventPublisher).publish(captor.capture());

        StudyProgressSaved event = captor.getValue();
        assertEquals(BATCH.toString(), event.eventId());    // the batch id, so retries can be skipped
        assertEquals(USER, event.userId());
        assertEquals(DECK, event.deckId());
        assertEquals(SESSION.toString(), event.sessionId());
        assertEquals(5, event.cardsReviewed());
        assertEquals(2, event.cardsFlagged());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("Another user's deck is not found: nothing saved, no event")
    void rejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class,
                () -> studyService.savePriorities(OTHER_USER, DECK, request(1, 0, new PriorityUpdate(1L, 99))));
        then(cardRepository).should(never()).raisePriority(anyLong(), anyLong(), anyInt(), any());
        then(eventPublisher).should(never()).publish(any());
    }

    @Test
    @DisplayName("More cards flagged than reviewed is rejected before anything is saved")
    void rejectsMoreFlaggedThanReviewed() {
        assertThrows(IllegalArgumentException.class,
                () -> studyService.savePriorities(USER, DECK, request(2, 3, new PriorityUpdate(1L, 13))));
        then(cardRepository).should(never()).raisePriority(anyLong(), anyLong(), anyInt(), any());
        then(eventPublisher).should(never()).publish(any());
    }

    @Test
    @DisplayName("If the broker can't be reached, the failure reaches the caller (so the browser retries)")
    void publishFailureIsReported() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);
        willThrow(new EventPublishException(new RuntimeException("broker down")))
                .given(eventPublisher).publish(any());

        assertThrows(EventPublishException.class,
                () -> studyService.savePriorities(USER, DECK, request(1, 0, new PriorityUpdate(1L, 13))));
    }
}
