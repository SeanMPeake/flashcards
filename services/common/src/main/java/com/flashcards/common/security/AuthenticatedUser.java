package com.flashcards.common.security;

// Who a valid token belongs to. A record is a compact immutable data class:
// the fields, constructor, getters (userId(), username()), equals and toString
// are all generated from this one line.
public record AuthenticatedUser(long userId, String username) {
}
