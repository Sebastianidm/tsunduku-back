package com.tsundoku.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRevocationService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    public void blacklistToken(String token, long remainingTimeMs) {
        if (remainingTimeMs <= 0) {
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(remainingTimeMs));
            log.info("Token añadido a la lista negra en Redis por {} ms", remainingTimeMs);
        } catch (Exception e) {
            log.error("Error al registrar token en la lista negra de Redis: ", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Error al consultar lista negra en Redis: ", e);
            return false;
        }
    }

    public void saveRefreshToken(Long userId, String refreshToken, long durationMs) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(durationMs));
        } catch (Exception e) {
            log.error("Error al guardar refresh token en Redis: ", e);
        }
    }

    public boolean isValidRefreshToken(Long userId, String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            String savedToken = redisTemplate.opsForValue().get(key);
            return refreshToken.equals(savedToken);
        } catch (Exception e) {
            log.error("Error al validar refresh token en Redis: ", e);
            return true; // En fallback de falla de cache, no bloqueamos la sesión si el JWT en sí es válido
        }
    }

    public void revokeRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error al revocar refresh token en Redis: ", e);
        }
    }
}
