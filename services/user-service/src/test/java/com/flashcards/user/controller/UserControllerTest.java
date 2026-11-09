package com.flashcards.user.controller;

import com.flashcards.user.dto.response.UserResponse;
import com.flashcards.user.exception.UsernameTakenException;
import com.flashcards.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

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
                .andExpect(jsonPath("$.username").value("SeanPeake"));
    }

    // Each case sits exactly on a length limit and breaks no rule.
    static Stream<Arguments> acceptedInputs() {
        return Stream.of(
                Arguments.of("shortest username (8)", "Abcdef_1", VALID_PASSWORD),
                Arguments.of("longest username (20)", "Abcdefghij_123456789", VALID_PASSWORD),
                Arguments.of("shortest password (10)", "SeanPeake", "Secret_pa1"),
                Arguments.of("longest password (64)", "SeanPeake", "A1_" + "a".repeat(61))
        );
    }

    @ParameterizedTest(name = "accepts {0}")
    @MethodSource("acceptedInputs")
    void acceptsInputsAtTheLimits(String label, String username, String password) throws Exception {
        given(userService.signup(any())).willReturn(new UserResponse(1L, username));

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(username, password)))
                .andExpect(status().isCreated());
    }

    // Each case breaks exactly one rule, so it can only fail for that rule's reason.
    static Stream<Arguments> rejectedInputs() {
        String composition = "password must include a letter, a digit, and a symbol, with no spaces";
        return Stream.of(
                Arguments.of("username too long (21)", "Abcdefghij_1234567890", VALID_PASSWORD,
                        "username must be 8-20 characters"),
                Arguments.of("password too short (9)", "SeanPeake", "Secret_p1",
                        "password must be 10-64 characters"),
                Arguments.of("password too long (65)", "SeanPeake", "A1_" + "a".repeat(62),
                        "password must be 10-64 characters"),
                Arguments.of("password missing a digit", "SeanPeake", "Secret_pass!", composition),
                Arguments.of("password missing a letter", "SeanPeake", "1234567_89!", composition)
        );
    }

    @ParameterizedTest(name = "rejects {0}")
    @MethodSource("rejectedInputs")
    void rejectsInputsBreakingOneRule(String label, String username, String password,
                                      String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(username, password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));

        then(userService).should(never()).signup(any());
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
