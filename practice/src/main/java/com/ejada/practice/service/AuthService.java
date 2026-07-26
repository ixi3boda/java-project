package com.ejada.practice.service;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;

/**
 * Service contract for authentication operations.
 * <p>Defines the public API for user registration and login.
 * The implementation ({@link com.ejada.practice.service.impl.AuthServiceImpl})
 * interacts with the user repository, role repository, password encoder,
 * and JWT utilities to fulfil these operations.</p>
 */
public interface AuthService {

    /**
     * Registers a new user account with the default {@code USER} role.
     * @param request the registration payload (username, email, password, first/last name)
     * @return a {@link UserResponse} representing the newly created account
     * @throws com.ejada.practice.exception.DuplicateResourceException if the username or email is already in use
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user and issues a JWT access token.
     * @param request the login credentials (username + password)
     * @return a {@link JwtResponse} containing the signed token and its expiry
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     * @throws org.springframework.security.authentication.LockedException if the account is temporarily locked
     */
    JwtResponse login(LoginRequest request);
}
