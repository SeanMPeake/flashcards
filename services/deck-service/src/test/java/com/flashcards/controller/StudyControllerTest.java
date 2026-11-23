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
import org.springframework.test.web.servlet.ResultActions;

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

    private static final String IDS =
            "\"batchId\":\"11111111-1111-1111-1111-111111111111\","
            + "\"sessionId\":\"22222222-2222-2222-2222-222222222222\",";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyService studyService;

    private ResultActions putProgress(String json) throws Exception {
        return mockMvc.perform(put("/api/study/1/priorities")
                .requestAttr(JwtAuthFilter.AUTHENTICATED_USER, USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // A valid body with the given priorities array.
    private String body(String priorities) {
        return "{" + IDS + "\"cardsReviewed\":2,\"cardsFlagged\":1,\"priorities\":" + priorities + "}";
    }

    @Test
    @DisplayName("PUT /api/study/{deckId}/priorities saves the batch and returns 204")
    void savesProgress() throws Exception {
        putProgress(body("[{\"cardId\":4,\"priority\":17},{\"cardId\":9,\"priority\":23}]"))
                .andExpect(status().isNoContent());

        then(studyService).should().savePriorities(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("A batch without a batchId returns 400")
    void rejectsMissingBatchId() throws Exception {
        putProgress("{\"sessionId\":\"22222222-2222-2222-2222-222222222222\","
                + "\"cardsReviewed\":1,\"cardsFlagged\":0,\"priorities\":[{\"cardId\":4,\"priority\":17}]}")
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("An empty priorities list returns 400")
    void rejectsEmptyBatch() throws Exception {
        putProgress(body("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A negative priority returns 400")
    void rejectsNegativePriority() throws Exception {
        putProgress(body("[{\"cardId\":4,\"priority\":-5}]"))
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A null entry in the batch returns 400, not 500")
    void rejectsNullEntry() throws Exception {
        putProgress(body("[null]"))
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("A priority above the upper bound returns 400")
    void rejectsPriorityAboveMaximum() throws Exception {
        putProgress(body("[{\"cardId\":4,\"priority\":2147483647}]"))
                .andExpect(status().isBadRequest());

        then(studyService).should(never()).savePriorities(any(), any(), any());
    }

    @Test
    @DisplayName("Another user's deck returns 404")
    void returnsNotFoundForAnotherUsersDeck() throws Exception {
        willThrow(new DeckNotFoundException(1L)).given(studyService).savePriorities(eq(1L), eq(1L), any());

        putProgress(body("[{\"cardId\":4,\"priority\":17}]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deck not found with id: 1"));
    }
}
