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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

    }

    @Override
    public JwtResponse login(LoginRequest request) {

    }

    private UserResponse toResponse(User user) {

    }
}
