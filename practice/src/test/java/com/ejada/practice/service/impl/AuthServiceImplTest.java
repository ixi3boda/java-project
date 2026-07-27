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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers login, registration, and lockout behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setFirstName("Alice");
        request.setLastName("Smith");
        return request;
    }

    /** register creates a USER-role account when username and email are free. */
    @Test
    @DisplayName("register creates a USER-role account when username and email are free")
    void register_success() {
        RegisterRequest request = registerRequest();
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getRoles()).containsExactly("USER");
    }

    /** register throws DuplicateResourceException for a taken username. */
    @Test
    @DisplayName("register throws DuplicateResourceException for a taken username")
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("alice");
        verify(userRepository, never()).save(any());
    }

    /** login issues a JWT and records success on valid credentials. */
    @Test
    @DisplayName("login issues a JWT and records success on valid credentials")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("password123");

        User user = User.builder().id(1L).username("alice").roles(Set.of(userRole)).build();
        CustomUserDetails principal = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(principal)).thenReturn("signed-jwt");
        when(jwtUtil.getExpirationMs()).thenReturn(3_600_000L);

        JwtResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("signed-jwt");
        assertThat(response.getExpiresIn()).isEqualTo(3_600_000L);
        verify(loginAttemptService).checkBlocked("alice");
        verify(loginAttemptService).loginSucceeded("alice");
        verify(loginAttemptService, never()).loginFailed(any());
    }

    /** login records a failure and rethrows on bad credentials. */
    @Test
    @DisplayName("login records a failure and rethrows on bad credentials")
    void login_badCredentials_recordsFailureAndRethrows() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptService).checkBlocked("alice");
        verify(loginAttemptService).loginFailed("alice");
        verify(loginAttemptService, never()).loginSucceeded(any());
    }

    /** login does not attempt authentication when the account is currently locked. */
    @Test
    @DisplayName("login does not attempt authentication when the account is currently locked")
    void login_locked_shortCircuits() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("password123");

        org.mockito.Mockito.doThrow(new org.springframework.security.authentication.LockedException("locked"))
                .when(loginAttemptService).checkBlocked("alice");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(org.springframework.security.authentication.LockedException.class);

        verify(authenticationManager, times(0)).authenticate(any());
    }
}
