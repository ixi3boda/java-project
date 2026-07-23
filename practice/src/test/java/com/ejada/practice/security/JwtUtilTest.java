package com.ejada.practice.security;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.ejada.practice.entity.Role;
import com.ejada.practice.entity.User;

class JwtUtilTest {
    @Test void createsAndValidatesToken() {
        JwtUtil jwt = new JwtUtil(); 
        ReflectionTestUtils.setField(jwt, "secret", "0123456789012345678901234567890123456789012345678901234567890123"); 
        ReflectionTestUtils.setField(jwt, "expirationMs", 60_000L);
        String token = jwt.generateToken(new CustomUserDetails(User.builder().username("ann").password("x").roles(Set.of(Role.builder().name("USER").build())).build()));
        assertTrue(jwt.isTokenValid(token)); 
        assertEquals("ann", jwt.extractUsername(token)); assertFalse(jwt.isTokenValid("not-a-token"));
    }
}
