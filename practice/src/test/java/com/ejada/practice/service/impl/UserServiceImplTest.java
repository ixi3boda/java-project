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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
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
 * Covers self-service profile updates and admin user management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role userRole;
    private User alice;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .roles(Set.of(userRole))
                .build();
    }

    /** getOwnProfile returns the mapped profile for an existing user. */
    @Test
    @DisplayName("getOwnProfile returns the mapped profile for an existing user")
    void getOwnProfile_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        UserResponse response = userService.getOwnProfile("alice");

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getRoles()).containsExactly("USER");
    }

    /** getOwnProfile throws ResourceNotFoundException when the user is missing. */
    @Test
    @DisplayName("getOwnProfile throws ResourceNotFoundException when the user is missing")
    void getOwnProfile_notFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOwnProfile("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /** updateOwnProfile updates name fields directly. */
    @Test
    @DisplayName("updateOwnProfile updates name fields directly")
    void updateOwnProfile_namesOnly() {
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();
        request.setFirstName("Alicia");
        request.setLastName("Smyth");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateOwnProfile("alice", request);

        assertThat(response.getFirstName()).isEqualTo("Alicia");
        assertThat(response.getLastName()).isEqualTo("Smyth");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    /** updateOwnProfile stages a pending email change and issues a verification token. */
    @Test
    @DisplayName("updateOwnProfile stages a pending email change and issues a verification token")
    void updateOwnProfile_emailChange_isPending() {
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();
        request.setEmail("new@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateOwnProfile("alice", request);

        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(alice.getPendingEmail()).isEqualTo("new@example.com");
        assertThat(alice.getEmailVerificationToken()).isNotBlank();
        assertThat(alice.getEmailVerificationExpiry()).isAfter(LocalDateTime.now());
    }

    /** updateOwnProfile throws DuplicateResourceException when the new email is already in use. */
    @Test
    @DisplayName("updateOwnProfile throws DuplicateResourceException when the new email is already in use")
    void updateOwnProfile_emailTaken_throws() {
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();
        request.setEmail("taken@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateOwnProfile("alice", request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    /** verifyEmail applies the pending email when the token is valid and unexpired. */
    @Test
    @DisplayName("verifyEmail applies the pending email when the token is valid and unexpired")
    void verifyEmail_success() {
        alice.setPendingEmail("new@example.com");
        alice.setEmailVerificationToken("tok-123");
        alice.setEmailVerificationExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByEmailVerificationToken("tok-123")).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.verifyEmail("tok-123");

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(alice.getPendingEmail()).isNull();
        assertThat(alice.getEmailVerificationToken()).isNull();
        assertThat(alice.getEmailVerificationExpiry()).isNull();
    }

    /** verifyEmail throws ResourceNotFoundException for an expired token. */
    @Test
    @DisplayName("verifyEmail throws ResourceNotFoundException for an expired token")
    void verifyEmail_expiredToken_throws() {
        alice.setPendingEmail("new@example.com");
        alice.setEmailVerificationToken("tok-123");
        alice.setEmailVerificationExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmailVerificationToken("tok-123")).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> userService.verifyEmail("tok-123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /** getAllUsers maps a page of entities to a page of responses. */
    @Test
    @DisplayName("getAllUsers maps a page of entities to a page of responses")
    void getAllUsers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(alice)));

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("alice");
    }

    /** getUserById returns the mapped user when found. */
    @Test
    @DisplayName("getUserById returns the mapped user when found")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    /** getUserById throws ResourceNotFoundException when not found. */
    @Test
    @DisplayName("getUserById throws ResourceNotFoundException when not found")
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private AdminUserCreateRequest createRequest() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("bob");
        request.setEmail("bob@example.com");
        request.setPassword("password123");
        request.setFirstName("Bob");
        request.setLastName("Jones");
        request.setRoles(Set.of("USER"));
        return request;
    }

    /** createUser resolves roles, hashes the password, and persists the account. */
    @Test
    @DisplayName("createUser resolves roles, hashes the password, and persists the account")
    void createUser_success() {
        AdminUserCreateRequest request = createRequest();

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(2L);
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getUsername()).isEqualTo("bob");
        assertThat(response.getRoles()).containsExactly("USER");
    }

    /** createUser throws DuplicateResourceException for a taken username. */
    @Test
    @DisplayName("createUser throws DuplicateResourceException for a taken username")
    void createUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    /** updateUser applies email, name, and role changes. */
    @Test
    @DisplayName("updateUser applies email, name, and role changes")
    void updateUser_fullUpdate_success() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setRoles(Set.of("ADMIN"));

        Role adminRole = Role.builder().id(2L).name("ADMIN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getFirstName()).isEqualTo("Updated");
        assertThat(response.getRoles()).containsExactly("ADMIN");
    }

    /** updateUser leaves fields unchanged when the request omits them. */
    @Test
    @DisplayName("updateUser leaves fields unchanged when the request omits them")
    void updateUser_partialUpdate_keepsExistingValues() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getFirstName()).isEqualTo("Alice");
        assertThat(response.getRoles()).containsExactly("USER");
        verify(userRepository, never()).existsByEmail(any());
        verify(roleRepository, never()).findByName(any());
    }

    /** updateUser throws ResourceNotFoundException when the user does not exist. */
    @Test
    @DisplayName("updateUser throws ResourceNotFoundException when the user does not exist")
    void updateUser_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new AdminUserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /** deleteUser removes an existing account. */
    @Test
    @DisplayName("deleteUser removes an existing account")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(alice);
    }

}
