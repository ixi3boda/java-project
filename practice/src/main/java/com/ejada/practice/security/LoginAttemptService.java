package com.ejada.practice.security;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks failed login attempts per username and applies a temporary lockout
 * after too many consecutive failures, as basic protection against
 * credential brute-forcing on the login endpoint.
 */
@Component
public class LoginAttemptService {

    /** Maximum number of consecutive failed logins before the account is locked. */
    private static final int MAX_ATTEMPTS = 5;

    /** Duration of the lockout window in seconds (15 minutes). */
    private static final long LOCKOUT_DURATION_SECONDS = 15 * 60;

    /** Per-username failure counters. */
    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    /** Per-username lockout expiry timestamps. */
    private final ConcurrentHashMap<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    /**
     * Checks whether the given username is currently locked out and throws
     * {@link LockedException} if so.
     *
     * <p>If the lockout window has expired, the stored state is cleared before
     * returning normally.</p>
     *
     * @param username the login identifier to check
     * @throws LockedException if the account is still within its lockout window
     */
    public void checkBlocked(String username) {
        Instant until = lockedUntil.get(username);
        if (until == null) {
            return;
        }
        if (Instant.now().isBefore(until)) {
            throw new LockedException(
                    "Account temporarily locked due to repeated failed login attempts. Please try again later.");
        }
        lockedUntil.remove(username);
        attempts.remove(username);
    }

    /**
     * Records a failed login attempt for the given username.
     *
     * <p>If the total count reaches {@value #MAX_ATTEMPTS}, a lockout is applied
     * starting from the current instant.</p>
     *
     * @param username the login identifier that failed authentication
     */
    public void loginFailed(String username) {
        int count = attempts.computeIfAbsent(username, k -> new AtomicInteger(0)).incrementAndGet();
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(username, Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS));
        }
    }

    /**
     * Clears all failure state for the given username after a successful login.
     *
     * @param username the login identifier that authenticated successfully
     */
    public void loginSucceeded(String username) {
        attempts.remove(username);
        lockedUntil.remove(username);
    }
}
