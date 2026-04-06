package com.flashcards.controller;

import com.flashcards.dto.response.CardResponse;
import com.flashcards.exception.CardNotFoundException;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.service.CardService;
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
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Web Layer Test — loads the Spring MVC slice (@WebMvcTest) to verify HTTP behavior.
// The service layer is mocked; the controller, routing, and exception handler are exercised together.
@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @DisplayName("POST /api/decks/{deckId}/cards creates card and returns 201")
    void createCardReturnsCreatedCard() throws Exception {
        CardResponse created = new CardResponse(1L, "What is JVM?", "Java Virtual Machine.", 0);

        given(cardService.createCard(eq(1L), any())).willReturn(created);

        mockMvc.perform(post("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"What is JVM?\",\"backText\":\"Java Virtual Machine.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.frontText").value("What is JVM?"))
                .andExpect(jsonPath("$.backText").value("Java Virtual Machine."));
    }

    @Test
    @DisplayName("POST /api/decks/{deckId}/cards returns 404 when deck is not found")
    void createCardReturnsNotFoundWhenDeckMissing() throws Exception {
        given(cardService.createCard(eq(999L), any())).willThrow(new DeckNotFoundException(999L));

        mockMvc.perform(post("/api/decks/999/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"Q\",\"backText\":\"A\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }

    @Test
    @DisplayName("PUT /api/decks/{deckId}/cards/{cardId} updates card and returns 200")
    void updateCardReturnsUpdatedCard() throws Exception {
        CardResponse updated = new CardResponse(1L, "Updated front", "Updated back", 0);

        given(cardService.updateCard(eq(1L), eq(1L), any())).willReturn(updated);

        mockMvc.perform(put("/api/decks/1/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"Updated front\",\"backText\":\"Updated back\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frontText").value("Updated front"))
                .andExpect(jsonPath("$.backText").value("Updated back"));
    }

    @Test
    @DisplayName("PUT /api/decks/{deckId}/cards/{cardId} returns 404 when card is not found")
    void updateCardReturnsNotFoundWhenCardMissing() throws Exception {
        given(cardService.updateCard(eq(1L), eq(999L), any())).willThrow(new CardNotFoundException(999L));

        mockMvc.perform(put("/api/decks/1/cards/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"Q\",\"backText\":\"A\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Card not found with id: 999"));
    }

    @Test
    @DisplayName("DELETE /api/decks/{deckId}/cards/{cardId} returns 204 when card is deleted")
    void deleteCardReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/decks/1/cards/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/decks/{deckId}/cards returns 400 when fields are blank")
    void createCardReturnsBadRequestWhenFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"\",\"backText\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/decks/{deckId}/cards/{cardId} returns 400 when fields are blank")
    void updateCardReturnsBadRequestWhenFieldsAreBlank() throws Exception {
        mockMvc.perform(put("/api/decks/1/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"\",\"backText\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("DELETE /api/decks/{deckId}/cards/{cardId} returns 404 when deck is not found")
    void deleteCardReturnsNotFoundWhenDeckMissing() throws Exception {
        willThrow(new DeckNotFoundException(999L)).given(cardService).deleteCard(999L, 1L);

        mockMvc.perform(delete("/api/decks/999/cards/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Deck not found with id: 999"));
    }

    @Test
    @DisplayName("DELETE /api/decks/{deckId}/cards/{cardId} returns 404 when card is not found")
    void deleteCardReturnsNotFoundWhenCardMissing() throws Exception {
        willThrow(new CardNotFoundException(999L)).given(cardService).deleteCard(1L, 999L);

        mockMvc.perform(delete("/api/decks/1/cards/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Card not found with id: 999"));
    }
}
