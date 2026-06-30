package com.school.platform.identityaccess.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "auth:login:attempts:";

    private final StringRedisTemplate redisTemplate;

    public void assertAllowed(String email, String clientIp) {
        try {
            if (isRateLimited(userKey(email)) || isRateLimited(ipKey(clientIp))) {
                throw tooManyRequests(email, clientIp, remainingSeconds(email, clientIp));
            }
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable for login rate limiting, allowing request: {}", ex.getMessage());
        }
    }

    public void registerFailure(String email, String clientIp) {
        try {
            long userAttempts = increment(userKey(email));
            long ipAttempts = increment(ipKey(clientIp));

            if (userAttempts >= MAX_ATTEMPTS || ipAttempts >= MAX_ATTEMPTS) {
                throw tooManyRequests(email, clientIp, remainingSeconds(email, clientIp));
            }
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable for login rate limiting, ignoring failure counter: {}", ex.getMessage());
        }
    }

    public void reset(String email, String clientIp) {
        try {
            redisTemplate.delete(userKey(email));
            redisTemplate.delete(ipKey(clientIp));
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable while clearing login attempts: {}", ex.getMessage());
        }
    }

    private boolean isRateLimited(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }

        long attempts = Long.parseLong(value);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            redisTemplate.expire(key, WINDOW);
        }

        return attempts >= MAX_ATTEMPTS;
    }

    private long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return 0L;
        }

        if (count == 1L) {
            redisTemplate.expire(key, WINDOW);
        }

        return count;
    }

    private long remainingSeconds(String email, String clientIp) {
        long userRemaining = remainingSeconds(userKey(email));
        long ipRemaining = remainingSeconds(ipKey(clientIp));

        if (userRemaining < 0) {
            return ipRemaining;
        }
        if (ipRemaining < 0) {
            return userRemaining;
        }
        if (userRemaining == 0) {
            return ipRemaining;
        }
        if (ipRemaining == 0) {
            return userRemaining;
        }
        return Math.min(userRemaining, ipRemaining);
    }

    private long remainingSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            return -1L;
        }
        return ttl;
    }

    private ResponseStatusException tooManyRequests(String email, String clientIp, long retryAfterSeconds) {
        String message = retryAfterSeconds > 0
                ? "Trop de tentatives de connexion. Reessayez dans " + retryAfterSeconds + " secondes."
                : "Trop de tentatives de connexion. Reessayez plus tard.";
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    private String userKey(String email) {
        return KEY_PREFIX + "user:" + hash(normalize(email));
    }

    private String ipKey(String clientIp) {
        return KEY_PREFIX + "ip:" + hash(normalize(clientIp));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
