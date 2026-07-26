package com.ejada.practice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility component for creating, validating, and parsing JSON Web Tokens (JWTs).
 *
 * <p>Tokens are signed with HMAC-SHA256 ({@code HS256}) using a secret key
 * loaded from the {@code app.jwt.secret} configuration property.  The secret
 * must be at least 64 characters long to satisfy the minimum key-length
 * requirement for HS256.</p>
 *
 * <p>Each token payload contains:
 * <ul>
 *   <li>{@code sub} – the user's username (used to reload the principal)</li>
 *   <li>{@code roles} – list of authority strings (e.g. {@code ["ROLE_USER"]})</li>
 *   <li>{@code iat} – issued-at timestamp</li>
 *   <li>{@code exp} – expiry timestamp</li>
 * </ul>
 * </p>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * The raw secret used to derive the HMAC signing key.
     * Injected from {@code app.jwt.secret}; must be ≥ 64 characters.
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Token lifetime in milliseconds.
     * Injected from {@code app.jwt.expiration-ms}; defaults to 3 600 000 (1 hour).
     */
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Derives an HMAC-SHA256 {@link SecretKey} from the configured secret string.
     *
     * @return the signing key; never {@code null}
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT for the given authenticated user.
     *
     * <p>The token subject is set to the username and a {@code roles} claim is
     * populated with the user's granted authority strings.</p>
     *
     * @param userDetails the authenticated user's security principal
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    /**
     * Extracts the username ({@code sub} claim) from a JWT.
     *
     * @param token a compact JWT string
     * @return the subject claim, i.e. the username; never {@code null}
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Checks whether a JWT is structurally valid, correctly signed, and not expired.
     *
     * <p>Any exception thrown during parsing (invalid signature, malformed token,
     * expired token, etc.) is caught and logged as a warning; the method returns
     * {@code false} in all such cases.</p>
     *
     * @param token a compact JWT string
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses a JWT and returns its claims payload.
     *
     * @param token a compact JWT string
     * @return the {@link Claims} object containing all token claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or tampered with
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns the configured token lifetime in milliseconds.
     * @return token expiration duration in milliseconds
     */
    public long getExpirationMs() {
        return expirationMs;
    }
}
