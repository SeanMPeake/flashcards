package com.flashcards.user.dto.response;

// Returned on successful login. The frontend keeps the token and sends it as
// "Authorization: Bearer <token>" on later requests.
public class LoginResponse {

    private final String token;
    private final String tokenType;
    private final long expiresIn;    // seconds
    private final UserResponse user;

    public LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }
}
