package com.flashcards.user.dto.response;

// Same {status, message} error shape as the deck service, so the frontend
// can handle errors from either service the same way.
public class ErrorResponse {

    private final int status;
    private final String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
