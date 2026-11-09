package com.flashcards.user.service;

import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.exception.UsernameTakenException;
import com.flashcards.user.model.User;
import com.flashcards.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

// Unit test — the repository is mocked; a real BCrypt encoder is used so the
// test proves the stored value is a genuine hash of the password.
class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, passwordEncoder);
        // saveAndFlush returns whatever it was given.
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(inv -> inv.getArgument(0));
    }

    private User captureSavedUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().saveAndFlush(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Signup stores a BCrypt hash, never the raw password")
    void storesHashNotRawPassword() {
        userService.signup(new SignupRequest("SeanPeake", "Secret_pass1"));

        User saved = captureSavedUser();
        assertNotEquals("Secret_pass1", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("Secret_pass1", saved.getPasswordHash()));
    }

    @Test
    @DisplayName("Signup keeps the username as typed and lowercases the comparison copy")
    void keepsDisplayCaseAndNormalizes() {
        var response = userService.signup(new SignupRequest("SeanPeake", "Secret_pass1"));

        User saved = captureSavedUser();
        assertEquals("SeanPeake", saved.getUsername());
        assertEquals("seanpeake", saved.getUsernameNormalized());
        assertEquals("SeanPeake", response.getUsername());
        then(userRepository).should().existsByUsernameNormalized("seanpeake");
    }

    @Test
    @DisplayName("Signup rejects a username that exists in any letter case")
    void rejectsExistingUsername() {
        given(userRepository.existsByUsernameNormalized("seanpeake")).willReturn(true);

        assertThrows(UsernameTakenException.class,
                () -> userService.signup(new SignupRequest("SEANPEAKE", "Secret_pass1")));
        then(userRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Signup reports a taken username when the database unique constraint fires")
    void handlesDatabaseRace() {
        given(userRepository.saveAndFlush(any(User.class)))
                .willThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(UsernameTakenException.class,
                () -> userService.signup(new SignupRequest("SeanPeake", "Secret_pass1")));
    }
}
