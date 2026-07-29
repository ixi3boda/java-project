package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.RoleRepository;
import com.ejada.practice.repository.UserRepository;
import com.ejada.practice.security.CustomUserDetails;
import com.ejada.practice.security.JwtUtil;
import com.ejada.practice.security.LoginAttemptService;
import com.ejada.practice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Handles user registration and authentication:
 * <ul>
 *   <li><b>Registration</b> – validates uniqueness of username and email,
 *       hashes the password with BCrypt, assigns the default {@code USER} role,
 *       and persists the new account.</li>
 *   <li><b>Login</b> – checks for brute-force lockout, delegates credential
 *       verification to Spring Security's {@link AuthenticationManager}, records
 *       success or failure with {@link LoginAttemptService}, and issues a JWT on
 *       successful authentication.</li>
 * </ul>
 * </p>
 */

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    /**
     * {@inheritDoc}
     *
     * <p>Assigns the default {@code USER} role; throws
     * {@link ResourceNotFoundException} if that role does not exist in the database.</p>
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(userRole))
                .build();

        return toResponse(userRepository.save(user));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Increments the failure counter on {@link BadCredentialsException} and
     * re-throws the exception so the global exception handler can map it to
     * {@code 401 Unauthorized}.</p>
     */
    @Override
    public JwtResponse login(LoginRequest request) {
        loginAttemptService.checkBlocked(request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            loginAttemptService.loginSucceeded(request.getUsername());

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            return JwtResponse.builder()
                    .accessToken(token)
                    .expiresIn(jwtUtil.getExpirationMs())
                    .build();
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(request.getUsername());
            throw e;
        }
    }

    /**
     * Converts a {@link User} entity to a {@link UserResponse} DTO.
     *
     * @param user the entity to convert; must not be {@code null}
     * @return the corresponding response DTO
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
