package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.RoleRepository;
import com.ejada.practice.repository.UserRepository;
import com.ejada.practice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock UserRepository users; 
    @Mock RoleRepository roles; 
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authenticationManager; 
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthServiceImpl service;

    @Test void registersUserWithDefaultRole() {
        RegisterRequest request = new RegisterRequest(); 
        request.setUsername("sam"); 
        request.setEmail("s@e.com");
        request.setPassword("password"); 
        request.setFirstName("Sam"); 
        request.setLastName("Doe");
        Role role = Role.builder().name("USER").build();
        when(roles.findByName("USER")).thenReturn(Optional.of(role)); 
        when(encoder.encode("password")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(i -> { User u = i.getArgument(0); u.setId(1L); return u; });
        var response = service.register(request);
        assertEquals("sam", response.getUsername()); 
        assertEquals(Set.of("USER"), response.getRoles());
        verify(users).save(argThat(u -> "encoded".equals(u.getPassword())));
    }

    @Test void rejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest(); 
        request.setUsername("sam");
        when(users.existsByUsername("sam")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.register(request));
        verifyNoInteractions(roles, encoder);
    }

    @Test void authenticatesAndReturnsJwt() {
        LoginRequest request = new LoginRequest(); 
        request.setUsername("sam"); 
        request.setPassword("password");
        User user = User.builder().username("sam").roles(Set.of(Role.builder().name("USER").build())).build();
        when(users.findByUsername("sam")).thenReturn(Optional.of(user)); when(jwtUtil.generateToken(any())).thenReturn("token");
        when(jwtUtil.getExpirationMs()).thenReturn(100L);
        var response = service.login(request);
        assertEquals("token", response.getAccessToken()); 
        assertEquals(100L, response.getExpiresIn()); 
        verify(authenticationManager).authenticate(any());
    }
}
