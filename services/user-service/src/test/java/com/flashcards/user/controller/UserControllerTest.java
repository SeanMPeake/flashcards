package com.flashcards.user.controller;

import com.flashcards.user.dto.response.UserResponse;
import com.flashcards.user.exception.UsernameTakenException;
import com.flashcards.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web Layer Test — loads the Spring MVC slice (@WebMvcTest) to verify HTTP behavior.
// The service layer is mocked; the controller, validation rules, and exception
// handler are exercised together.
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private static final String VALID_PASSWORD = "Secret_pass1";

    private String body(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    @DisplayName("POST /api/users/signup returns 201 with the new user")
    void signupReturnsCreated() throws Exception {
        given(userService.signup(any())).willReturn(new UserResponse(1L, "SeanPeake"));

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SeanPeake", VALID_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("SeanPeake"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Signup rejects a username shorter than 8 characters")
    void rejectsShortUsername() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("sean", VALID_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("username must be 8-20 characters"));

        then(userService).should(never()).signup(any());
    }

    @Test
    @DisplayName("Signup rejects a username containing a symbol")
    void rejectsUsernameWithSymbol() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("sean.peake", VALID_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("username may contain only letters, numbers, and underscores"));
    }

    @Test
    @DisplayName("Signup rejects a password with no symbol")
    void rejectsPasswordWithoutSymbol() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SeanPeake", "Secretpass1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("password must include a letter, a digit, and a symbol, with no spaces"));
    }

    @Test
    @DisplayName("Signup rejects a password containing a space")
    void rejectsPasswordWithSpace() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SeanPeake", "Secret pass1!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("password must include a letter, a digit, and a symbol, with no spaces"));
    }

    @Test
    @DisplayName("Signup returns 409 when the username is taken")
    void returnsConflictWhenUsernameTaken() throws Exception {
        given(userService.signup(any())).willThrow(new UsernameTakenException());

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SeanPeake", VALID_PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }
}
