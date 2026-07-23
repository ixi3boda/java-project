package com.ejada.practice.service;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getOwnProfile(String username);
    UserResponse updateOwnProfile(String username, UserSelfUpdateRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse createUser(AdminUserCreateRequest request);
    UserResponse updateUser(Long id, AdminUserUpdateRequest request);
    void deleteUser(Long id);
}
