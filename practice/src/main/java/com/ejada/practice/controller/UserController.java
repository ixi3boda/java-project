package com.ejada.practice.controller;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management.
 * <p>Provides two groups of operations:
 * <ol>
 *   <li><b>Self-service</b> – any authenticated user can read/update their own
 *       profile and verify an e-mail change.</li>
 *   <li><b>Admin</b> – users with the {@code ADMIN} role can list all users,
 *       fetch any user by ID, create new accounts, update any account, and
 *       delete accounts.</li>
 * </ol>
 * </p>
 * <p>Base path: {@code /api/users}</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /** Service that encapsulates user management business logic. */
    private final UserService userService;

    /**
     * Returns the profile of the currently authenticated user.
     * @param authentication the current authenticated user (injected by Spring Security)
     * @return {@code 200 OK} containing the user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getOwnProfile(authentication.getName()));
    }

    /**
     * Updates the authenticated user's own profile (first name, last name, e-mail).
     * <p>If a new e-mail is provided, it is placed in a pending state and a
     * verification token is issued; the live e-mail is not changed until the
     * token is confirmed via {@code GET /api/users/verify-email}.</p>
     * @param authentication the current authenticated user
     * @param request        the fields to update (all fields are optional)
     * @return {@code 200 OK} containing the updated profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(Authentication authentication,
                                                          @Valid @RequestBody UserSelfUpdateRequest request) {
        return ResponseEntity.ok(userService.updateOwnProfile(authentication.getName(), request));
    }

    /**
     * Confirms a pending e-mail address change using a one-time verification token.
     * <p>This endpoint is publicly accessible (no JWT required) so that the user can
     * click the link in the verification e-mail before they are logged in.</p>
     * @param token the verification token issued when the e-mail change was requested
     * @return {@code 200 OK} containing the updated profile with the new active e-mail
     */
    @GetMapping("/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(userService.verifyEmail(token));
    }

    /**
     * Returns a paginated list of all registered users (admin only).
     * @param pageable pagination and sorting parameters
     * @return {@code 200 OK} containing a page of user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    /**
     * Retrieves any user by their ID (admin only).
     * @param id the user's primary key
     * @return {@code 200 OK} containing the user's profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Creates a new user account with an explicitly specified role set (admin only).
     * @param request the creation payload (username, email, password, name, roles)
     * @return {@code 201 Created} containing the newly created user's profile
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    /**
     * Updates any user account (admin only).
     * <p>Admins can change e-mail, name, and roles. An e-mail change here takes
     * effect immediately (without the verification step required for self-service).</p>
     * @param id      the user's primary key
     * @param request the fields to update (all fields are optional)
     * @return {@code 200 OK} containing the updated profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                     @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Permanently deletes a user account (admin only).
     * @param id the user's primary key
     * @return {@code 204 No Content} on successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
