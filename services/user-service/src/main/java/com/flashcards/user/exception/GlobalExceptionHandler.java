package com.flashcards.user.exception;

import com.flashcards.user.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.List;

// Centralizes error handling so all API error responses share the same
// {status, message} shape as the deck service.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // When several rules fail, report them in a fixed order so the same input always
    // gets the same message: fields top to bottom, and for one field the most basic
    // rule first (missing, then wrong length, then wrong characters).
    private static final List<String> FIELD_ORDER = List.of("username", "password");
    private static final List<String> RULE_ORDER = List.of("NotBlank", "Size", "Pattern");

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorResponse> handleUsernameTaken(UsernameTakenException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // Returns the first failed rule as "<field> <message>", e.g. "password must be 10-64 characters".
        String message = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparingInt((FieldError e) -> rank(FIELD_ORDER, e.getField()))
                        .thenComparingInt(e -> rank(RULE_ORDER, e.getCode())))
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request.");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    // Client mistakes that Spring detects before a controller runs. Without these
    // they would fall through to the catch-all below and be reported as 500s.

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Not found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full exception server-side; return a generic message so
        // internal details are not leaked to the client.
        logger.error("Unhandled exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

    // Position in the given order; anything not listed goes last.
    private static int rank(List<String> order, String value) {
        int index = order.indexOf(value);
        return index < 0 ? order.size() : index;
    }
}
