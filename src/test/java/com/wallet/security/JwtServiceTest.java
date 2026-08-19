package com.wallet.security;

import com.wallet.entity.Role;
import com.wallet.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "super_secret_jwt_key_that_is_at_least_256_bits_long_for_hmac_sha!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationInSeconds", 3600L);
    }

    @Test
    @DisplayName("Generate token contains correct subject and role claims")
    void generateToken_Success() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractRole(token));
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void isTokenValid_TamperedToken_ReturnsFalse() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);
        String tamperedToken = token + "corrupted";

        assertFalse(jwtService.isTokenValid(tamperedToken));
    }
}
