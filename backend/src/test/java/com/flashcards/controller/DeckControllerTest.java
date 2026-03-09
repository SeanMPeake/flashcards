package com.flashcards.controller;

import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.model.Deck;
import com.flashcards.repository.DeckRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeckController.class)
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckRepository deckRepository;

    @Test
    @DisplayName("GET /api/decks returns all decks")
    void getDecksReturnsAllDecks() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Deck deck1 = new Deck("Java Basics", "Core Java review cards", now, now);
        Deck deck2 = new Deck("SDLC Terms", "Software development life cycle concepts", now, now);

        given(deckRepository.findAll()).willReturn(List.of(deck1, deck2));

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java Basics"))
                .andExpect(jsonPath("$[1].name").value("SDLC Terms"));
    }

    @Test
    @DisplayName("GET /api/decks returns empty list when no decks exist")
    void getDecksReturnsEmptyList() throws Exception {
        given(deckRepository.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/decks/{id} returns deck when found")
    void getDeckByIdReturnsDeck() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Deck deck = new Deck("SQL Basics", "Intro database review", now, now);

        given(deckRepository.findById(1L)).willReturn(Optional.of(deck));

        mockMvc.perform(get("/api/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("SQL Basics"))
                .andExpect(jsonPath("$.description").value("Intro database review"));
    }

    @Test
    @DisplayName("GET /api/decks/{id} returns 404 when deck is not found")
    void getDeckByIdReturnsNotFound() throws Exception {
        given(deckRepository.findById(999L))
                .willThrow(new DeckNotFoundException(999L));

        mockMvc.perform(get("/api/decks/999"))
                .andExpect(status().isNotFound());
    }
}