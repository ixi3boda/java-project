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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getOwnProfile(String username) {
        return toResponse(findByUsername(username));
    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(String username, UserSelfUpdateRequest request) {
        User user = findByUsername(username);
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateResourceException("Email is already in use");
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName())) user.setLastName(request.getLastName());
        return toResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new DuplicateResourceException("Username is already in use");
        if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateResourceException("Email is already in use");
        User user = User.builder().username(request.getUsername()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).firstName(request.getFirstName())
                .lastName(request.getLastName()).roles(resolveRoles(request.getRoles())).build();
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = findById(id);
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateResourceException("Email is already in use");
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName())) user.setLastName(request.getLastName());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) user.setRoles(resolveRoles(request.getRoles()));
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(findById(id));
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())).build();
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream().map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                .collect(Collectors.toSet());
    }
}
