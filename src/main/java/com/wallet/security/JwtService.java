package com.wallet.security;

import com.wallet.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private long expirationInSeconds;

    /**
     * Converts our configured secret string into a cryptographic SecretKey.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for the authenticated user.
     * Sets:
     * - Subject = user ID (String)
     * - Claim "role" = user's role name (USER / ADMIN)
     * - Expiration = 1 hour (3600 seconds)
     */
    public String generateToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + (expirationInSeconds * 1000)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parses and validates the token signature, returning the claims payload.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the User ID from the token's subject.
     */
    public Long extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Extracts the Role from the token's claims.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Checks if the token is valid (correct signature and not expired).
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationInSeconds() {
        return expirationInSeconds;
    }
}
