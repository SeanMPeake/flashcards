package com.flashcards.controller;

import com.flashcards.dto.response.CardResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.exception.EmptyDeckException;
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
@WebMvcTest(StudyController.class)
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyService studyService;

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns first card")
    void startSessionReturnsFirstCard() throws Exception {
        CardResponse card = new CardResponse(1L, "What is JVM?", "Java Virtual Machine.", 1);
        given(studyService.startSession(1L)).willReturn(card);

        mockMvc.perform(get("/api/study/1/start"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.frontText").value("What is JVM?"))
                .andExpect(jsonPath("$.priority").value(1));
    }

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns 404 when deck not found")
    void startSessionReturnsNotFoundWhenDeckMissing() throws Exception {
        given(studyService.startSession(999L)).willThrow(new DeckNotFoundException(999L));

        mockMvc.perform(get("/api/study/999/start"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/study/{deckId}/start returns 400 when deck has no cards")
    void startSessionReturnsBadRequestWhenDeckEmpty() throws Exception {
        given(studyService.startSession(1L)).willThrow(new EmptyDeckException(1L));

        mockMvc.perform(get("/api/study/1/start"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/study/{deckId}/next returns next card")
    void nextCardReturnsNextCard() throws Exception {
        CardResponse card = new CardResponse(2L, "What is JRE?", "Java Runtime Environment.", 31);
        given(studyService.nextCard(eq(1L), any())).willReturn(card);

        mockMvc.perform(post("/api/study/1/next")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":1,\"markForReview\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
