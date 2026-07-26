package com.ejada.practice.security;

import com.ejada.practice.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Adapts the application's {@link User} entity to the Spring Security
 * {@link UserDetails} contract.
 *
 * <p>Each {@link com.ejada.practice.entity.Role} stored on the user is
 * converted to a {@link SimpleGrantedAuthority} with the prefix {@code "ROLE_"}
 * (e.g. a role named {@code "ADMIN"} becomes {@code "ROLE_ADMIN"}) so that
 * {@code @PreAuthorize("hasRole('ADMIN')")} expressions work correctly.</p>
 */
public class CustomUserDetails implements UserDetails {

    /** The underlying domain user whose data backs this security principal. */
    private final User user;

    /**
     * Constructs a {@code CustomUserDetails} wrapping the given {@link User}.
     *
     * @param user the domain user; must not be {@code null}
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the database primary key of the wrapped {@link User}.
     *
     * @return the user's ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Returns the granted authorities derived from the user's roles.
     *
     * @return collection of granted authorities; never {@code null}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the BCrypt-encoded password hash of the user.
     *
     * @return the encoded password; never {@code null}
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the unique username used to authenticate the user.
     *
     * @return the username; never {@code null}
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Indicates whether the account has expired.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is locked.
     *
     * <p>Delegates to {@link User#isAccountNonLocked()}, which is set to
     * {@code false} by {@link LoginAttemptService} after too many failed logins.</p>
     *
     * @return {@code true} if the account is not locked; {@code false} otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    /**
     * Indicates whether the credentials (password) have expired.
     *
     * @return {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     *
     * @return {@code true} if the account is enabled; {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
