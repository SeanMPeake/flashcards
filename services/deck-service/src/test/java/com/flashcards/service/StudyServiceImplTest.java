package com.flashcards.service;

import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.dto.request.SavePrioritiesRequest.PriorityUpdate;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import com.flashcards.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

// Service Unit Test — StudyServiceImpl saves a batch of study progress and stores a
// StudyProgressSaved event for it in the outbox.
//
// The never-lower, same-deck-only rule lives in one atomic database UPDATE
// (CardRepository.raisePriority); these tests check the service hands every entry to
// it for the right deck and owner. That the database itself rejects lower values,
// including under simultaneous saves, was checked live against Postgres. That the
// outbox row and the priorities commit together comes from @Transactional.
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
    private OutboxEventRepository outboxRepository;

    // A real mapper, so the tests also check the stored JSON reads back as the event.
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private StudyServiceImpl studyService;

    @BeforeEach
    void createService() {
        studyService = new StudyServiceImpl(cardRepository, deckRepository, outboxRepository, jsonMapper);
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
    @DisplayName("A card listed twice is offered both values; the raise-only update keeps the higher")
    void duplicateCardOffersBothValues() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(2, 0, new PriorityUpdate(4L, 40), new PriorityUpdate(4L, 17)));

        then(cardRepository).should().raisePriority(eq(DECK), eq(4L), eq(40), any());
        then(cardRepository).should().raisePriority(eq(DECK), eq(4L), eq(17), any());
    }

    @Test
    @DisplayName("Stores a StudyProgressSaved event describing the batch in the outbox")
    void storesEventInOutbox() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(5, 2, new PriorityUpdate(1L, 13)));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        then(outboxRepository).should().save(captor.capture());
        OutboxEvent row = captor.getValue();
        String expectedEventId = USER + ":" + BATCH;           // user + batch, so repeats can be skipped
        assertEquals(expectedEventId, row.getEventId());

        StudyProgressSaved event = jsonMapper.readValue(row.getPayload(), StudyProgressSaved.class);
        assertEquals(expectedEventId, event.eventId());
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
        then(outboxRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("More cards flagged than reviewed is rejected before anything is saved")
    void rejectsMoreFlaggedThanReviewed() {
        assertThrows(IllegalArgumentException.class,
                () -> studyService.savePriorities(USER, DECK, request(2, 3, new PriorityUpdate(1L, 13))));
        then(cardRepository).should(never()).raisePriority(anyLong(), anyLong(), anyInt(), any());
        then(outboxRepository).should(never()).save(any());
    }
}
