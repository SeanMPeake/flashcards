package com.flashcards.controller;

import com.flashcards.common.security.AuthenticatedUser;
import com.flashcards.exception.DeckNotFoundException;
import com.flashcards.security.JwtAuthFilter;
import com.flashcards.service.StudyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web Layer Test — loads the Spring MVC slice (@WebMvcTest) to verify HTTP behavior.
// The service layer is mocked; the controller, validation, and exception handler are exercised together.
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

    private org.springframework.test.web.servlet.ResultActions putPriorities(String json) throws Exception {
        return mockMvc.perform(put("/api/study/1/priorities")
                .requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    @DisplayName("PUT /api/study/{deckId}/priorities saves the batch and returns 204")
    void savesPriorities() throws Exception {
        putPriorities("{\"priorities\":[{\"cardId\":4,\"priority\":17},{\"cardId\":9,\"priority\":23}]}")
                .andExpect(status().isNoContent());

        then(studyService).should().savePriorities(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("An empty batch returns 400")
    void rejectsEmptyBatch() throws Exception {
        putPriorities("{\"priorities\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A negative priority returns 400")
    void rejectsNegativePriority() throws Exception {
        putPriorities("{\"priorities\":[{\"cardId\":4,\"priority\":-5}]}")
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A null entry in the batch returns 400, not 500")
    void rejectsNullEntry() throws Exception {
        putPriorities("{\"priorities\":[null]}")
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A priority above the upper bound returns 400")
    void rejectsPriorityAboveMaximum() throws Exception {
        putPriorities("{\"priorities\":[{\"cardId\":4,\"priority\":2147483647}]}")
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("Another user's deck returns 404")
    void returnsNotFoundForAnotherUsersDeck() throws Exception {
        willThrow(new DeckNotFoundException(1L)).given(studyService).savePriorities(eq(1L), eq(1L), any());

        putPriorities("{\"priorities\":[{\"cardId\":4,\"priority\":17}]}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deck not found with id: 1"));
    }
}
