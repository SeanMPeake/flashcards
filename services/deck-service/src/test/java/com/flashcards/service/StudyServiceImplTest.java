package com.flashcards.service;

import com.flashcards.dto.request.SavePrioritiesRequest;
import com.flashcards.dto.request.SavePrioritiesRequest.PriorityUpdate;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.repository.CardRepository;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

// Service Unit Test — StudyServiceImpl only saves priorities sent by the browser.
//
// The never-lower, same-deck-only rule lives in one atomic database UPDATE
// (CardRepository.raisePriority); these tests check the service hands every entry
// to it, scoped to the right deck, and only for the deck's owner. That the database
// itself rejects a lower value, including under simultaneous saves, was checked live
// against Postgres rather than in a unit test.
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

    @Test
    @DisplayName("savePriorities applies each entry through the deck-scoped, raise-only update")
    void raisesEachPriorityWithinTheDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(1L, 13), new PriorityUpdate(2L, 8)));

        then(cardRepository).should().raisePriority(eq(DECK), eq(1L), eq(13), any());
        then(cardRepository).should().raisePriority(eq(DECK), eq(2L), eq(8), any());
    }

    @Test
    @DisplayName("A card listed twice is offered both values; the raise-only update keeps the higher")
    void duplicateCardOffersBothValues() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, USER)).willReturn(true);

        studyService.savePriorities(USER, DECK, request(new PriorityUpdate(4L, 40), new PriorityUpdate(4L, 17)));

        then(cardRepository).should().raisePriority(eq(DECK), eq(4L), eq(40), any());
        then(cardRepository).should().raisePriority(eq(DECK), eq(4L), eq(17), any());
    }

    @Test
    @DisplayName("savePriorities treats another user's deck as not found and changes nothing")
    void rejectsAnotherUsersDeck() {
        given(deckRepository.existsByIdAndOwnerUserId(DECK, OTHER_USER)).willReturn(false);

        assertThrows(DeckNotFoundException.class,
                () -> studyService.savePriorities(OTHER_USER, DECK, request(new PriorityUpdate(1L, 99))));
        then(cardRepository).should(never()).raisePriority(anyLong(), anyLong(), anyInt(), any());
    }
}
