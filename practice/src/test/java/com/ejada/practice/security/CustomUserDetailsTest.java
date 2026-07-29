package com.ejada.practice.security;

import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the UserDetails adapter around the User entity.
 */
@DisplayName("CustomUserDetails")
class CustomUserDetailsTest {

    /** getAuthorities prefixes every role name with ROLE_. */
    @Test
    @DisplayName("getAuthorities prefixes every role name with ROLE_")
    void getAuthorities_prefixesRoles() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        Role adminRole = Role.builder().id(2L).name("ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("alice")
                .password("hashed")
                .roles(Set.of(userRole, adminRole))
                .enabled(true)
                .accountNonLocked(true)
                .build();

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    /** delegate accessors reflect the underlying user's state. */
    @Test
    @DisplayName("delegate accessors reflect the underlying user's state")
    void delegateAccessors_reflectUnderlyingUser() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        User user = User.builder()
                .id(7L)
                .username("bob")
                .password("secret-hash")
                .roles(Set.of(userRole))
                .enabled(false)
                .accountNonLocked(false)
                .build();

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getId()).isEqualTo(7L);
        assertThat(details.getUsername()).isEqualTo("bob");
        assertThat(details.getPassword()).isEqualTo("secret-hash");
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }
}
