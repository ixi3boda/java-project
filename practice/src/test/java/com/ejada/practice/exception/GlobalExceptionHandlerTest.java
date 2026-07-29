package com.ejada.practice.exception;

import com.ejada.practice.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks that each exception type maps to the right HTTP status and body.
 */
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    /** handleValidation joins field errors and returns 400. */
    @Test
    @DisplayName("handleValidation joins field errors and returns 400")
    void handleValidation_returns400WithJoinedMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "name", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("name: must not be blank");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    /** handleNotFound returns 404 with the exception message. */
    @Test
    @DisplayName("handleNotFound returns 404 with the exception message")
    void handleNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product not found with id: 1");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found with id: 1");
    }

    /** handleDuplicate returns 409 with the exception message. */
    @Test
    @DisplayName("handleDuplicate returns 409 with the exception message")
    void handleDuplicate_returns409() {
        DuplicateResourceException ex = new DuplicateResourceException("Username already taken: bob");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Username already taken: bob");
    }

    /** handleAccessDenied returns 403 with the exception message. */
    @Test
    @DisplayName("handleAccessDenied returns 403 with the exception message")
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("You do not have permission to view this order");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /** handleBadCredentials returns 401 with a generic message. */
    @Test
    @DisplayName("handleBadCredentials returns 401 with a generic message")
    void handleBadCredentials_returns401WithGenericMessage() {
        BadCredentialsException ex = new BadCredentialsException("actual reason should not leak");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid username or password");
    }

    /** handleGeneric returns 500 for any unhandled exception. */
    @Test
    @DisplayName("handleGeneric returns 500 for any unhandled exception")
    void handleGeneric_returns500() {
        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }
}
