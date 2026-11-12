package com.flashcards.user.service;

import com.flashcards.user.dto.request.LoginRequest;
import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.dto.response.LoginResponse;
import com.flashcards.user.exception.InvalidCredentialsException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

// Unit test — the repository is mocked; a real BCrypt encoder and a real JwtService
// are used, so the tests prove genuine hashing and genuine tokens.
class UserServiceImplTest {

    private static final String TEST_SECRET = "test-only-secret-at-least-32-bytes-long!";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        // spy = the real encoder, but Mockito can also check how it was called.
        passwordEncoder = spy(new BCryptPasswordEncoder());
        jwtService = new JwtService(TEST_SECRET, 24);
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtService);
        // saveAndFlush returns whatever it was given.
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(inv -> inv.getArgument(0));
    }

    private User captureSavedUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().saveAndFlush(captor.capture());
        return captor.getValue();
    }

    // A stored user with id 1, as the database would return it.
    private User storedUser(String username, String rawPassword) {
        User user = new User(username, username.toLowerCase(), new BCryptPasswordEncoder().encode(rawPassword));
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    // ----- signup -----

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
        // Free at the first check; taken by the time the insert lands.
        given(userRepository.existsByUsernameNormalized("seanpeake")).willReturn(false, true);
        given(userRepository.saveAndFlush(any(User.class)))
                .willThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(UsernameTakenException.class,
                () -> userService.signup(new SignupRequest("SeanPeake", "Secret_pass1")));
    }

    @Test
    @DisplayName("Signup passes on other database errors instead of calling them 'taken'")
    void otherConstraintErrorsAreNotReportedAsTaken() {
        // The name is free before and after: the failure is something else.
        given(userRepository.existsByUsernameNormalized("seanpeake")).willReturn(false);
        given(userRepository.saveAndFlush(any(User.class)))
                .willThrow(new DataIntegrityViolationException("value too long"));

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.signup(new SignupRequest("SeanPeake", "Secret_pass1")));
    }

    // ----- login -----

    @Test
    @DisplayName("Login with the right password returns a token for that user")
    void loginReturnsTokenForUser() {
        given(userRepository.findByUsernameNormalized("seanpeake"))
                .willReturn(Optional.of(storedUser("SeanPeake", "Secret_pass1")));

        LoginResponse response = userService.login(new LoginRequest("SeanPeake", "Secret_pass1"));

        assertEquals("Bearer", response.getTokenType());
        assertEquals(24 * 60 * 60, response.getExpiresIn());
        assertEquals(1L, response.getUser().getId());
        assertEquals("SeanPeake", response.getUser().getUsername());
        // The token is genuine: it verifies with the secret and names user 1.
        assertEquals("1", JwtServiceTest.parse(response.getToken(), TEST_SECRET).getSubject());
    }

    @Test
    @DisplayName("Login matches the username regardless of letter case")
    void loginIgnoresUsernameCase() {
        given(userRepository.findByUsernameNormalized("seanpeake"))
                .willReturn(Optional.of(storedUser("SeanPeake", "Secret_pass1")));

        LoginResponse response = userService.login(new LoginRequest("SEANpeake", "Secret_pass1"));

        assertEquals("SeanPeake", response.getUser().getUsername());
    }

    @Test
    @DisplayName("Login ignores spaces around the username")
    void loginTrimsUsername() {
        given(userRepository.findByUsernameNormalized("seanpeake"))
                .willReturn(Optional.of(storedUser("SeanPeake", "Secret_pass1")));

        LoginResponse response = userService.login(new LoginRequest("  SeanPeake ", "Secret_pass1"));

        assertEquals("SeanPeake", response.getUser().getUsername());
    }

    @Test
    @DisplayName("Login treats a password over 72 bytes as wrong credentials, not a server error")
    void loginRejectsOverlongPassword() {
        String overlong = "A1_" + "a".repeat(97);   // 100 bytes; BCrypt would throw

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(new LoginRequest("SeanPeake", overlong)));
        then(userRepository).should(never()).findByUsernameNormalized(anyString());
    }

    @Test
    @DisplayName("Login rejects a wrong password")
    void loginRejectsWrongPassword() {
        given(userRepository.findByUsernameNormalized("seanpeake"))
                .willReturn(Optional.of(storedUser("SeanPeake", "Secret_pass1")));

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(new LoginRequest("SeanPeake", "Wrong_pass9")));
    }

    @Test
    @DisplayName("Login rejects an unknown username but still runs a password check")
    void loginRejectsUnknownUserWithTimingSafeguard() {
        given(userRepository.findByUsernameNormalized("nobodyhere")).willReturn(Optional.empty());
        // Ignore the encode() call made at startup for the dummy hash.
        clearInvocations(passwordEncoder);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(new LoginRequest("NobodyHere", "Secret_pass1")));

        // The timing safeguard: a BCrypt check runs even though no user was found.
        then(passwordEncoder).should().matches(eq("Secret_pass1"), anyString());
    }
}
