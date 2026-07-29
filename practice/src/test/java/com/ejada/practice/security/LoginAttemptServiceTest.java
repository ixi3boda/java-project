package com.ejada.practice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the failed-login lockout logic.
 */
@DisplayName("LoginAttemptService")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    /** checkBlocked does not throw for a username with no recorded attempts. */
    @Test
    @DisplayName("checkBlocked does not throw for a username with no recorded attempts")
    void checkBlocked_unknownUser_noThrow() {
        assertThatCode(() -> loginAttemptService.checkBlocked("alice")).doesNotThrowAnyException();
    }

    /** checkBlocked does not throw after fewer than the maximum failed attempts. */
    @Test
    @DisplayName("checkBlocked does not throw after fewer than the maximum failed attempts")
    void checkBlocked_belowThreshold_noThrow() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed("alice");
        }

        assertThatCode(() -> loginAttemptService.checkBlocked("alice")).doesNotThrowAnyException();
    }

    /** checkBlocked throws LockedException after reaching the maximum failed attempts. */
    @Test
    @DisplayName("checkBlocked throws LockedException after reaching the maximum failed attempts")
    void checkBlocked_atThreshold_throwsLocked() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("alice");
        }

        assertThatThrownBy(() -> loginAttemptService.checkBlocked("alice"))
                .isInstanceOf(LockedException.class);
    }

    /** loginSucceeded clears any accumulated failure count. */
    @Test
    @DisplayName("loginSucceeded clears any accumulated failure count")
    void loginSucceeded_clearsFailures() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed("alice");
        }

        loginAttemptService.loginSucceeded("alice");

        loginAttemptService.loginFailed("alice");
        assertThatCode(() -> loginAttemptService.checkBlocked("alice")).doesNotThrowAnyException();
    }

    /** failures for different usernames are tracked independently. */
    @Test
    @DisplayName("failures for different usernames are tracked independently")
    void loginFailed_isPerUsername() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("alice");
        }

        assertThatCode(() -> loginAttemptService.checkBlocked("bob")).doesNotThrowAnyException();
    }
}
