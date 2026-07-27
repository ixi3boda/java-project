package com.ejada.practice.controller;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes public authentication endpoints.
 *
 * <p>All routes under {@code /api/auth} are permit-all in the security
 * configuration, so no JWT is required to call them.</p>
 *
 * <p>Base path: {@code /api/auth}</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Service that encapsulates registration and login business logic. */
    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * <p>The new account is assigned the default {@code USER} role.
     * The request body is validated via Bean Validation; any violations
     * result in a {@code 400 Bad Request} response.</p>
     *
     * @param request the registration payload (username, email, password, name)
     * @return {@code 201 Created} containing the newly created user's profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a user and returns a signed JWT access token.
     *
     * <p>Repeated failed attempts will trigger a temporary account lockout
     * (see {@link com.ejada.practice.security.LoginAttemptService}).
     * A successful login resets the failure counter.</p>
     *
     * @param request the login credentials (username + password)
     * @return {@code 200 OK} containing the JWT token and its expiry duration
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
