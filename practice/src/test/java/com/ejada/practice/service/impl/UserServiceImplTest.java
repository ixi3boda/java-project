package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.AdminUserCreateRequest;
import com.ejada.practice.dto.request.UserSelfUpdateRequest;
import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.RoleRepository;
import com.ejada.practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock UserRepository users; 
    @Mock RoleRepository roles; 
    @Mock PasswordEncoder encoder;
    @InjectMocks UserServiceImpl service;

    @Test void adminCanCreateUser() {
        AdminUserCreateRequest r = new AdminUserCreateRequest(); 
        r.setUsername("ann"); 
        r.setEmail("a@e.com"); 
        r.setPassword("password"); 
        r.setFirstName("Ann"); 
        r.setLastName("Lee"); 
        r.setRoles(Set.of("ADMIN"));
        when(roles.findByName("ADMIN")).thenReturn(Optional.of(Role.builder().name("ADMIN").build())); 
        when(encoder.encode("password")).thenReturn("hash");
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(Set.of("ADMIN"), service.createUser(r).getRoles());
    }
    @Test void ownProfileUpdateRejectsUsedEmail() {
        User user = User.builder().username("ann").email("old@e.com").roles(Set.of()).build();
        when(users.findByUsername("ann")).thenReturn(Optional.of(user)); 
        when(users.existsByEmail("used@e.com")).thenReturn(true);
        UserSelfUpdateRequest r = new UserSelfUpdateRequest(); 
        r.setEmail("used@e.com");
        assertThrows(DuplicateResourceException.class, () -> service.updateOwnProfile("ann", r));
    }
}
