package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.RoleRepository;
import com.ejada.practice.repository.UserRepository;
import com.ejada.practice.security.CustomUserDetails;
import com.ejada.practice.security.JwtUtil;
import com.ejada.practice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Required USER role does not exist"));
        User user = User.builder()
                .username(request.getUsername()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName()).lastName(request.getLastName())
                .roles(Set.of(userRole)).build();
        return toResponse(userRepository.save(user));
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        String token = jwtUtil.generateToken(new CustomUserDetails(user));
        return JwtResponse.builder().accessToken(token).expiresIn(jwtUtil.getExpirationMs()).build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())).build();
    }
}
