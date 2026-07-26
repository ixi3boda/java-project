package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.RoleRepository;
import com.ejada.practice.repository.UserRepository;
import com.ejada.practice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int EMAIL_VERIFICATION_VALID_HOURS = 24;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getOwnProfile(String username) {

    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(String username, UserSelfUpdateRequest request) {

    }

    @Override
    @Transactional
    public UserResponse verifyEmail(String token) {
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

    }

    @Override
    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {

    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {

    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

    }

    private Set<Role> resolveRoles(Set<String> roleNames) {

    }

    private User findByUsername(String username) {

    }

    private User findById(Long id) {

    }

    private UserResponse toResponse(User user) {

    }
}
