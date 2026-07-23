package com.ejada.practice.controller;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock private AuthService authService;
    @InjectMocks private AuthController controller;

    @Test void registerCreatesUser() {
        RegisterRequest request = new RegisterRequest();
        UserResponse user = UserResponse.builder().username("sam").build();
        when(authService.register(request)).thenReturn(user);

        var response = controller.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(user, response.getBody());
        verify(authService).register(request);
    }

    @Test void loginReturnsJwt() {
        LoginRequest request = new LoginRequest();
        JwtResponse token = JwtResponse.builder().accessToken("token").build();
        when(authService.login(request)).thenReturn(token);

        assertSame(token, controller.login(request).getBody());
        verify(authService).login(request);
    }
}
