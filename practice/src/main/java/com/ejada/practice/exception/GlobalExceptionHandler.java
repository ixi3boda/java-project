package com.ejada.practice.exception;

import com.ejada.practice.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralised exception-to-HTTP-response mapping for the entire application.
 *
 * <p>Annotated with {@link RestControllerAdvice} so it intercepts exceptions thrown
 * by any {@code @RestController} and translates them into consistent
 * {@link ErrorResponse} JSON payloads.  Each handler method corresponds to a
 * specific exception type and returns an appropriate HTTP status code.</p>
 *
 * <p>Mapping overview:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → {@code 400 Bad Request}</li>
 *   <li>{@link ResourceNotFoundException}       → {@code 404 Not Found}</li>
 *   <li>{@link DuplicateResourceException}      → {@code 409 Conflict}</li>
 *   <li>{@link InsufficientStockException}      → {@code 400 Bad Request}</li>
 *   <li>{@link AccessDeniedException}           → {@code 403 Forbidden}</li>
 *   <li>{@link BadCredentialsException}         → {@code 401 Unauthorized}</li>
 *   <li>{@link LockedException}                → {@code 429 Too Many Requests}</li>
 *   <li>{@link Exception} (catch-all)           → {@code 500 Internal Server Error}</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean-validation failures on request body / path / query parameters.
     *
     * <p>Collects all field-level constraint violations into a single
     * semicolon-separated message.</p>
     *
     * @param ex      the validation exception raised by the framework
     * @param request the current HTTP request (used to extract the URI)
     * @return {@code 400 Bad Request} with a concatenated validation message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Handles lookup failures for entities that do not exist.
     *
     * @param ex      the exception carrying a descriptive not-found message
     * @param request the current HTTP request
     * @return {@code 404 Not Found}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles attempts to create a resource that already exists (e.g. duplicate username).
     *
     * @param ex      the exception carrying a descriptive conflict message
     * @param request the current HTTP request
     * @return {@code 409 Conflict}
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles order placement failures caused by insufficient product inventory.
     *
     * @param ex      the exception carrying a stock-shortage description
     * @param request the current HTTP request
     * @return {@code 400 Bad Request}
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles Spring Security access-denied events (authenticated but not authorised).
     *
     * @param ex      the access-denied exception
     * @param request the current HTTP request
     * @return {@code 403 Forbidden}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handles authentication failures caused by wrong credentials.
     *
     * <p>Returns a generic message to avoid leaking whether the username exists.</p>
     *
     * @param ex      the bad-credentials exception
     * @param request the current HTTP request
     * @return {@code 401 Unauthorized}
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }

    /**
     * Handles account lock-outs triggered after repeated failed login attempts.
     *
     * @param ex      the locked exception
     * @param request the current HTTP request
     * @return {@code 429 Too Many Requests}
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, WebRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    /**
     * Catch-all handler for any unhandled {@link Exception}.
     *
     * <p>Logs and surfaces the raw message; in production consider replacing
     * with a generic "Internal server error" to avoid leaking implementation details.</p>
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return {@code 500 Internal Server Error}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /**
     * Constructs a {@link ResponseEntity} wrapping an {@link ErrorResponse} body.
     *
     * @param status  the HTTP status to set on the response
     * @param message the error detail to include in the body
     * @param request the current HTTP request (used to extract the request URI)
     * @return a fully populated {@link ResponseEntity}
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
