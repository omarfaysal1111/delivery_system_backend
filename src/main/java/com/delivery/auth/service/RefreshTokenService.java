package com.delivery.auth.service;

import com.delivery.auth.jwt.JwtProperties;
import com.delivery.common.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    private static final String PREFIX = "rt:";

    public String create(UUID userId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(
                PREFIX + token,
                userId.toString(),
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiryMs())
        );
        return token;
    }

    /**
     * Validates the token, deletes it (rotation), and returns the owner's user ID.
     */
    public UUID validateAndRotate(String token) {
        String key = PREFIX + token;
        String userId = redis.opsForValue().get(key);
        if (userId == null) {
            throw new AuthException("Refresh token not found or expired");
        }
        redis.delete(key);
        return UUID.fromString(userId);
    }

    public void revoke(String token) {
        redis.delete(PREFIX + token);
    }

    /**
     * Logout from all devices — scans all refresh tokens belonging to this user.
     * KEYS is acceptable here since this is a low-frequency admin-path operation.
     */
    public void revokeAll(UUID userId) {
        var keys = redis.keys(PREFIX + "*");
        if (keys == null) return;
        keys.stream()
                .filter(k -> userId.toString().equals(redis.opsForValue().get(k)))
                .forEach(redis::delete);
    }
}
