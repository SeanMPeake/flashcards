package com.flashcards.exception;

import com.flashcards.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centralizes error handling so all API error responses share a consistent shape.
// Each @ExceptionHandler method maps one exception type to an HTTP status and an
// ErrorResponse body, replacing Spring's default error format.
//
// Note: RFC 7807 Problem Details (ProblemDetail) was considered for this layer but
// was not implemented. For a demo project with a single frontend consumer, the
// simpler {status, message} shape is sufficient. Problem Details would be the
// appropriate choice if this API were to be made publicly available.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeckNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeckNotFound(DeckNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Use a generic message for unhandled exceptions to avoid leaking
        // internal error details or stack trace information to the client.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred."));
    }
}
