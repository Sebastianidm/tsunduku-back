package com.tsundoku.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "super_secret_jwt_key_that_is_at_least_256_bits_long_tsundoku_test_environment_2026";
    private final long expirationMs = 3600000; // 1 hr
    private final long refreshExpirationMs = 86400000; // 1 day

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs, refreshExpirationMs);
    }

    @Test
    @DisplayName("Debe generar y validar correctamente un Access Token")
    void generateAndValidateAccessToken() {
        Long userId = 1L;
        String email = "test@tsundoku.com";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtTokenProvider.generateAccessToken(userId, email, roles);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Debe generar y validar correctamente un Refresh Token")
    void generateAndValidateRefreshToken() {
        Long userId = 100L;

        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(refreshToken));
    }

    @Test
    @DisplayName("Debe fallar al validar un token con firma alterada")
    void failValidationForInvalidSignature() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@tsundoku.com", List.of("ROLE_USER"));
        String tamperedToken = token + "invalid";

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Debe rechazar secretos de menos de 256 bits en la inicialización")
    void rejectShortSecretKey() {
        assertThrows(IllegalArgumentException.class, () ->
                new JwtTokenProvider("short_secret", expirationMs, refreshExpirationMs));
    }
}
