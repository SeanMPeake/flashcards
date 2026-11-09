package com.flashcards.user.exception;

// Thrown for both an unknown username and a wrong password. One message for both
// cases, so the login form cannot be used to discover which usernames exist.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
