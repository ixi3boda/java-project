package com.ejada.practice.service;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for user management operations.
 * <p>Splits user operations into two groups:
 * <ul>
 *   <li><b>Self-service</b> – methods a regular user calls for their own account.</li>
 *   <li><b>Admin</b> – methods restricted to administrators.</li>
 * </ul>
 * The implementation is {@link com.ejada.practice.service.impl.UserServiceImpl}.</p>
 */
public interface UserService {

    /**
     * Returns the profile of the user identified by the given username.
     * @param username the username of the authenticated user
     * @return a {@link UserResponse} representing the user's profile
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the user is not found
     */
    UserResponse getOwnProfile(String username);

    /**
     * Updates the profile of the user identified by the given username.
     * <p>If a new e-mail is provided, it enters a pending state and a
     * verification token is generated; the active e-mail is not changed
     * until the token is confirmed.</p>
     * @param username the username of the authenticated user
     * @param request  the fields to update (all optional)
     * @return a {@link UserResponse} reflecting the applied changes
     * @throws com.ejada.practice.exception.ResourceNotFoundException  if the user is not found
     * @throws com.ejada.practice.exception.DuplicateResourceException if the requested e-mail is already in use
     */
    UserResponse updateOwnProfile(String username, UserSelfUpdateRequest request);

    /**
     * Confirms a pending e-mail change using a one-time verification token.
     * @param token the verification token issued when the e-mail change was requested
     * @return a {@link UserResponse} with the newly active e-mail address
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the token is invalid or expired
     */
    UserResponse verifyEmail(String token);

    /**
     * Returns a paginated list of all registered users (admin).
     * @param pageable pagination and sorting parameters
     * @return a {@link Page} of {@link UserResponse} objects
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Retrieves any user by their primary key (admin).
     * @param id the user's primary key
     * @return a {@link UserResponse} representing the user
     * @throws com.ejada.practice.exception.ResourceNotFoundException if no user with the given ID exists
     */
    UserResponse getUserById(Long id);

    /**
     * Creates a new user account with an explicitly specified role set (admin).
     * @param request the creation payload (username, email, password, name, roles)
     * @return a {@link UserResponse} representing the newly created account
     * @throws com.ejada.practice.exception.DuplicateResourceException if the username or email is already in use
     * @throws com.ejada.practice.exception.ResourceNotFoundException  if any specified role name does not exist
     */
    UserResponse createUser(AdminUserCreateRequest request);

    /**
     * Updates any user account (admin).
     * <p>E-mail changes applied by an admin take effect immediately without
     * requiring e-mail verification.</p>
     * @param id      the user's primary key
     * @param request the fields to update (all optional)
     * @return a {@link UserResponse} reflecting the applied changes
     * @throws com.ejada.practice.exception.ResourceNotFoundException  if the user or a role is not found
     * @throws com.ejada.practice.exception.DuplicateResourceException if the requested e-mail is already in use
     */
    UserResponse updateUser(Long id, AdminUserUpdateRequest request);

    /**
     * Permanently deletes a user account (admin).
     * @param id the user's primary key
     * @throws com.ejada.practice.exception.ResourceNotFoundException if no user with the given ID exists
     */
    void deleteUser(Long id);
}
