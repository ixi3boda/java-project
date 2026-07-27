package com.ejada.practice.security;

import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers token generation and validation edge cases.
 */
@DisplayName("JwtUtil")
class JwtUtilTest {

    private static final String TEST_SECRET = "Q3o/8FviJiLZRJk4Uv0Dv0Xy13bFLHqnmH5wdsbtmHQ=";

    private JwtUtil jwtUtil;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3_600_000L);

        Role role = Role.builder().id(1L).name("USER").build();
        User user = User.builder().id(1L).username("alice").roles(Set.of(role)).build();
        userDetails = new CustomUserDetails(user);
    }

    /** getExpirationMs returns the configured value. */
    @Test
    @DisplayName("getExpirationMs returns the configured value")
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(3_600_000L);
    }

    /** generateToken produces a token whose subject matches the username. */
    @Test
    @DisplayName("generateToken produces a token whose subject matches the username")
    void generateToken_extractUsername_roundTrip() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
    }

    /** isTokenValid returns true for a freshly generated token. */
    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    void isTokenValid_freshToken_true() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    /** isTokenValid returns false for a malformed token. */
    @Test
    @DisplayName("isTokenValid returns false for a malformed token")
    void isTokenValid_malformedToken_false() {
        assertThat(jwtUtil.isTokenValid("not-a-valid-jwt")).isFalse();
    }

    /** isTokenValid returns false for an expired token. */
    @Test
    @DisplayName("isTokenValid returns false for an expired token")
    void isTokenValid_expiredToken_false() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String expiredToken = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }

    /** isTokenValid returns false for a token signed with a different secret. */
    @Test
    @DisplayName("isTokenValid returns false for a token signed with a different secret")
    void isTokenValid_wrongSignature_false() {
        String token = jwtUtil.generateToken(userDetails);

        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "secret",
                "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
        ReflectionTestUtils.setField(otherUtil, "expirationMs", 3_600_000L);

        assertThat(otherUtil.isTokenValid(token)).isFalse();
    }
}
