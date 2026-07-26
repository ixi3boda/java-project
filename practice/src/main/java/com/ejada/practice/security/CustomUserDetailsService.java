package com.ejada.practice.security;

import com.ejada.practice.entity.User;
import com.ejada.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security {@link UserDetailsService} implementation that loads
 * {@link CustomUserDetails} from the database.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository used to fetch the user and their associated roles. */
    private final UserRepository userRepository;

    /**
     * Loads the user identified by {@code username} and wraps it in a
     * {@link CustomUserDetails} instance.
     *
     * @param username the username to look up (case-sensitive, as stored in the DB)
     * @return a fully populated {@link UserDetails} backed by the domain {@link User}
     * @throws UsernameNotFoundException if no user with that username exists
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
        return new CustomUserDetails(user);
    }
}
