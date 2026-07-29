package com.ejada.practice.security;

import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import com.ejada.practice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Covers loading users by username for Spring Security.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    /** loadUserByUsername returns a CustomUserDetails wrapping the found user. */
    @Test
    @DisplayName("loadUserByUsername returns a CustomUserDetails wrapping the found user")
    void loadUserByUsername_found() {
        Role role = Role.builder().id(1L).name("USER").build();
        User user = User.builder().id(1L).username("alice").roles(Set.of(role)).build();
        when(userRepository.findByUsernameWithRoles("alice")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo("alice");
    }

    /** loadUserByUsername throws UsernameNotFoundException when the user does not exist. */
    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException when the user does not exist")
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByUsernameWithRoles("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
