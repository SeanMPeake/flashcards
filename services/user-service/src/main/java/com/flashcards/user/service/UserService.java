package com.flashcards.user.service;

import com.flashcards.user.dto.request.LoginRequest;
import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.dto.response.LoginResponse;
import com.flashcards.user.dto.response.UserResponse;

public interface UserService {

    // Creates a new account. Throws UsernameTakenException if the name is in use,
    // ignoring case.
    UserResponse signup(SignupRequest request);

    // Checks the username (ignoring case) and password and returns a signed token.
    // Throws InvalidCredentialsException if either is wrong.
    LoginResponse login(LoginRequest request);
}
