package com.ejada.practice.service;

import com.ejada.practice.dto.request.LoginRequest;
import com.ejada.practice.dto.request.RegisterRequest;
import com.ejada.practice.dto.response.JwtResponse;
import com.ejada.practice.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
}
