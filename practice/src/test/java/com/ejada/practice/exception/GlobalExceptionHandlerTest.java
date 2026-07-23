package com.ejada.practice.exception;

import com.ejada.practice.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @Mock private WebRequest request;
    @Mock private BindingResult bindingResult;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test void mapsApplicationAndSecurityExceptionsToExpectedResponses() {
        when(request.getDescription(false)).thenReturn("uri=/api/items");

        assertResponse(handler.handleNotFound(new ResourceNotFoundException("missing"), request), 404, "missing");
        assertResponse(handler.handleDuplicate(new DuplicateResourceException("exists"), request), 409, "exists");
        assertResponse(handler.handleInsufficientStock(new InsufficientStockException("low stock"), request), 400, "low stock");
        assertResponse(handler.handleAccessDenied(new AccessDeniedException("denied"), request), 403, "denied");
        assertResponse(handler.handleBadCredentials(new BadCredentialsException("bad"), request), 401, "Invalid username or password");
        assertResponse(handler.handleGeneric(new RuntimeException("broken"), request), 500, "broken");
    }

    @Test void mapsValidationErrors() {
        when(request.getDescription(false)).thenReturn("uri=/api/items");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("product", "name", "must not be blank")));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        assertResponse(handler.handleValidation(exception, request), 400, "name: must not be blank");
    }

    private void assertResponse(org.springframework.http.ResponseEntity<ErrorResponse> response, int status, String message) {
        assertEquals(status, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().getMessage());
        assertEquals("/api/items", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }
}
