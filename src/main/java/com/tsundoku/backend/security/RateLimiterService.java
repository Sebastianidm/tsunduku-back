package com.tsundoku.backend.security;

import com.tsundoku.backend.common.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewAuthBucket() {
        // Límite de 10 peticiones por minuto por dirección IP en endpoints sensibles (Auth / Registration)
        Refill refill = Refill.greedy(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    public void checkAuthRateLimit(String clientIp) {
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewAuthBucket());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Has superado el límite de intentos de autenticación. Intenta de nuevo en 1 minuto.");
        }
    }
}
