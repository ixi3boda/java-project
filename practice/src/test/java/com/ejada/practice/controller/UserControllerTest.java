package com.ejada.practice.controller;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.exception.GlobalExceptionHandler;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers profile, admin, and lookup endpoints for users.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    private UserResponse sampleUser() {
        return UserResponse.builder()
                .id(1L).username("alice").email("alice@example.com")
                .firstName("Alice").lastName("Smith").roles(Set.of("USER"))
                .build();
    }

    /** GET /api/users/me returns the caller's own profile. */
    @Test
    @DisplayName("GET /api/users/me returns the caller's own profile")
    void getMyProfile_returns200() throws Exception {
        when(userService.getOwnProfile("alice")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    /** PUT /api/users/me updates the caller's own profile. */
    @Test
    @DisplayName("PUT /api/users/me updates the caller's own profile")
    void updateMyProfile_returns200() throws Exception {
        UserSelfUpdateRequest request = new UserSelfUpdateRequest();
        request.setFirstName("Alicia");

        when(userService.updateOwnProfile(eq("alice"), any(UserSelfUpdateRequest.class)))
                .thenReturn(sampleUser());

        mockMvc.perform(put("/api/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null, List.of()))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /** GET /api/users/verify-email confirms a pending email change. */
    @Test
    @DisplayName("GET /api/users/verify-email confirms a pending email change")
    void verifyEmail_returns200() throws Exception {
        when(userService.verifyEmail("tok-123")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/verify-email").param("token", "tok-123"))
                .andExpect(status().isOk());
    }

    /** GET /api/users returns a page of all users (admin). */
    @Test
    @DisplayName("GET /api/users returns a page of all users (admin)")
    void getAllUsers_returns200() throws Exception {
        when(userService.getAllUsers(any()))
                .thenReturn(new PageImpl<>(List.of(sampleUser()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    /** GET /api/users/{id} returns the requested user (admin). */
    @Test
    @DisplayName("GET /api/users/{id} returns the requested user (admin)")
    void getUserById_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    /** GET /api/users/{id} returns 404 when the user does not exist. */
    @Test
    @DisplayName("GET /api/users/{id} returns 404 when the user does not exist")
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    /** POST /api/users creates a new account (admin) and returns 201. */
    @Test
    @DisplayName("POST /api/users creates a new account (admin) and returns 201")
    void createUser_returns201() throws Exception {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("bob");
        request.setEmail("bob@example.com");
        request.setPassword("password123");
        request.setFirstName("Bob");
        request.setLastName("Jones");
        request.setRoles(Set.of("USER"));

        when(userService.createUser(any(AdminUserCreateRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /** POST /api/users returns 400 for an invalid payload. */
    @Test
    @DisplayName("POST /api/users returns 400 for an invalid payload")
    void createUser_invalidPayload_returns400() throws Exception {
        AdminUserCreateRequest request = new AdminUserCreateRequest();

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /** PUT /api/users/{id} updates the target account (admin). */
    @Test
    @DisplayName("PUT /api/users/{id} updates the target account (admin)")
    void updateUser_returns200() throws Exception {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setFirstName("Updated");

        when(userService.updateUser(eq(1L), any(AdminUserUpdateRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /** DELETE /api/users/{id} returns 204. */
    @Test
    @DisplayName("DELETE /api/users/{id} returns 204")
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
