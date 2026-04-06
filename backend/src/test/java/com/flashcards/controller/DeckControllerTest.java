package com.flashcards.controller;

import com.flashcards.dto.response.CardResponse;
import com.flashcards.dto.response.DeckResponse;
import com.flashcards.dto.response.DeckSummaryResponse;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.service.DeckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Web Layer Test — loads the Spring MVC slice (@WebMvcTest) to verify HTTP behavior.
// The service layer is mocked; the controller, routing, and exception handler are exercised together.
@WebMvcTest(DeckController.class)
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    @Test
    @DisplayName("GET /api/decks returns all decks")
    void getDecksReturnsAllDecks() throws Exception {
        DeckSummaryResponse deck1 = new DeckSummaryResponse(1L, "Java Basics", "Core Java review cards", 2);
        DeckSummaryResponse deck2 = new DeckSummaryResponse(2L, "SDLC Terms", "Software development life cycle concepts", 0);

        given(deckService.getDecks()).willReturn(List.of(deck1, deck2));

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java Basics"))
                .andExpect(jsonPath("$[0].cardCount").value(2))
                .andExpect(jsonPath("$[0].cards").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("SDLC Terms"));
    }

    @Test
    @DisplayName("GET /api/decks returns empty list when no decks exist")
    void getDecksReturnsEmptyList() throws Exception {
        given(deckService.getDecks()).willReturn(List.of());

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/decks/{id} returns deck when found")
    void getDeckByIdReturnsDeck() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CardResponse card = new CardResponse(1L, "What does SQL stand for?", "Structured Query Language.", 1);
        DeckResponse deck = new DeckResponse(1L, "SQL Basics", "Intro database review", now, now, 1, List.of(card));

        given(deckService.getDeckById(1L)).willReturn(deck);

        mockMvc.perform(get("/api/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("SQL Basics"))
                .andExpect(jsonPath("$.description").value("Intro database review"))
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].backText").value("Structured Query Language."));
    }

    @Test
    @DisplayName("GET /api/decks/{id} returns 404 with error body when deck is not found")
    void getDeckByIdReturnsNotFound() throws Exception {
        given(deckService.getDeckById(999L)).willThrow(new DeckNotFoundException(999L));

        mockMvc.perform(get("/api/decks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/decks/{id} returns 500 with generic error body on unexpected exception")
    void getDeckByIdReturnsInternalServerError() throws Exception {
        given(deckService.getDeckById(1L)).willThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get("/api/decks/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    @Test
    @DisplayName("POST /api/decks creates deck and returns 201")
    void createDeckReturnsCreatedDeck() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DeckResponse created = new DeckResponse(1L, "Java Basics", "Core Java review cards", now, now, 0, List.of());

        given(deckService.createDeck(any())).willReturn(created);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java Basics\",\"description\":\"Core Java review cards\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Basics"));
    }

    @Test
    @DisplayName("PUT /api/decks/{id} updates deck and returns 200")
    void updateDeckReturnsUpdatedDeck() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DeckResponse updated = new DeckResponse(1L, "Updated Name", "Updated description", now, now, 0, List.of());

        given(deckService.updateDeck(eq(1L), any())).willReturn(updated);

        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/decks/{id} returns 204 when deck is deleted")
    void deleteDeckReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/decks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/decks returns 400 when name is blank")
    void createDeckReturnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Some description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/decks/{id} returns 400 when name is blank")
    void updateDeckReturnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Some description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("DELETE /api/decks/{id} returns 404 when deck is not found")
    void deleteDeckReturnsNotFound() throws Exception {
        willThrow(new DeckNotFoundException(999L)).given(deckService).deleteDeck(999L);

        mockMvc.perform(delete("/api/decks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }
}
