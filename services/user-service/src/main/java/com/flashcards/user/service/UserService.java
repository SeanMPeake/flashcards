package com.flashcards.user.service;

import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.dto.response.UserResponse;

public interface UserService {

    // Creates a new account. Throws UsernameTakenException if the name is in use,
    // ignoring case.
    UserResponse signup(SignupRequest request);
}
