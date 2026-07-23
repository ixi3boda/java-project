package com.ejada.practice.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.AdminUserUpdateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.dto.response.UserResponse;
import com.ejada.practice.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock private UserService userService;
    @Mock private Authentication authentication;
    @InjectMocks private UserController controller;

    @Test void getMyProfileUsesAuthenticatedUsername() {
        var user = new UserResponse(); 
        when(authentication.getName()).thenReturn("sam"); 
        when(userService.getOwnProfile("sam")).thenReturn(user);
        assertSame(user, controller.getMyProfile(authentication).getBody());
    }
    @Test void updateMyProfileUsesAuthenticatedUsername() {
        var request = new UserSelfUpdateRequest(); 
        var user = new UserResponse(); 
        when(authentication.getName()).thenReturn("sam"); 
        when(userService.updateOwnProfile("sam", request)).thenReturn(user);
        assertSame(user, controller.updateMyProfile(authentication, request).getBody());
    }
    @Test void getAllUsersReturnsPage() {
        var pageable = PageRequest.of(0, 10); 
        var page = new PageImpl<>(List.of(new UserResponse())); 
        when(userService.getAllUsers(pageable)).thenReturn(page);
        assertSame(page, controller.getAllUsers(pageable).getBody());
    }
    @Test void getUserByIdReturnsUser() {
        var user = new UserResponse(); 
        when(userService.getUserById(4L)).thenReturn(user);
        assertSame(user, controller.getUserById(4L).getBody());
    }
    @Test void createUserReturnsCreatedUser() {
        var request = new AdminUserCreateRequest(); 
        when(userService.createUser(request)).thenReturn(new UserResponse());
        assertEquals(201, controller.createUser(request).getStatusCode().value());
    }
    @Test void updateUserReturnsUpdatedUser() {
        var request = new AdminUserUpdateRequest(); 
        var user = new UserResponse(); when(userService.updateUser(4L, request)).thenReturn(user);
        assertSame(user, controller.updateUser(4L, request).getBody());
    }
    @Test void deleteUserReturnsNoContent() {
        assertEquals(204, controller.deleteUser(4L).getStatusCode().value()); 
        verify(userService).deleteUser(4L);
    }
}
