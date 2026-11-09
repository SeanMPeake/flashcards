package com.flashcards.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    // 8-20 characters: letters, numbers, and underscores only.
    @NotBlank
    @Size(min = 8, max = 20, message = "must be 8-20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "may contain only letters, numbers, and underscores")
    private String username;

    // 10-64 characters with at least one letter, one digit, and one symbol.
    // Each (?=...) checks that one kind of character appears somewhere.
    // [\x21-\x7E] allows only printable ASCII (no spaces, no accented characters),
    // so 64 characters is always 64 bytes, safely under BCrypt's 72-byte limit.
    @NotBlank
    @Size(min = 10, max = 64, message = "must be 10-64 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])[\\x21-\\x7E]+$",
             message = "must include a letter, a digit, and a symbol, with no spaces")
    private String password;

    public SignupRequest() {
    }

    public SignupRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
