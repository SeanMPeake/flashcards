package com.flashcards.common.security;

// Thrown for any token that cannot be trusted: expired, tampered with, signed with
// a different secret, or not a token at all. Callers only need to know it is bad.
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired token");
    }
}
