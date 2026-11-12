package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.StudyStartResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EmptyDeckException;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.StudyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Web Layer Test — loads the Spring MVC slice (@WebMvcTest) to verify HTTP behavior.
// The service layer is mocked; the controller, routing, and exception handler are exercised together.
//
// The token filter is not part of this slice (it has its own tests), so each request
// carries the logged-in user directly, the way the filter attaches it.
@WebMvcTest(StudyController.class)
class StudyControllerTest {

    private static final AuthenticatedUser USER = new AuthenticatedUser(1L, "SeanPeake");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyService studyService;

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns first card")
    void startSessionReturnsFirstCard() throws Exception {
        CardResponse card = new CardResponse(1L, "What is JVM?", "Java Virtual Machine.", 1);
        given(studyService.startSession(1L, 1L)).willReturn(new StudyStartResponse(card, 12));

        mockMvc.perform(get("/api/study/1/start").requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.card.id").value(1))
                .andExpect(jsonPath("$.card.frontText").value("What is JVM?"))
                .andExpect(jsonPath("$.card.priority").value(1))
                .andExpect(jsonPath("$.totalCards").value(12));
    }

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns 404 when deck not found")
    void startSessionReturnsNotFoundWhenDeckMissing() throws Exception {
        given(studyService.startSession(1L, 999L)).willThrow(new DeckNotFoundException(999L));

        mockMvc.perform(get("/api/study/999/start").requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns 400 when deck has no cards")
    void startSessionReturnsBadRequestWhenDeckEmpty() throws Exception {
        given(studyService.startSession(1L, 1L)).willThrow(new EmptyDeckException(1L));

        mockMvc.perform(get("/api/study/1/start").requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/study/{deckId}/next returns next card")
    void nextCardReturnsNextCard() throws Exception {
        CardResponse card = new CardResponse(2L, "What is JRE?", "Java Runtime Environment.", 31);
        given(studyService.nextCard(eq(1L), eq(1L), any())).willReturn(card);

        mockMvc.perform(post("/api/study/1/next")
                        .requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardId\":1,\"priority\":1,\"markForReview\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.frontText").value("What is JRE?"))
                .andExpect(jsonPath("$.priority").value(31));
    }

    @Test
    @DisplayName("POST /api/study/{deckId}/next returns 400 when cardId is missing")
    void nextCardReturnsBadRequestWhenCardIdMissing() throws Exception {
        mockMvc.perform(post("/api/study/1/next")
                        .requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":1,\"markForReview\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
