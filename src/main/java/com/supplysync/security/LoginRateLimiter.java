package com.supplysync.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimiter {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;



    public boolean isAllowed(String ip) {
        String key = "rate-limit:login:" + ip;
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null && Integer.parseInt(value) >= MAX_ATTEMPTS) return false;
        } catch (Exception e) {
            log.warn("Redis unavailable for rate limit check: {}", e.getMessage());
        }
        return true;
    }

    public void recordFailedAttempt(String ip) {
        String key = "rate-limit:login:" + ip;
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    operations.opsForValue().increment((K) key);
                    operations.expire((K) key, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                    return null;
                }
            });
        } catch (Exception e) {
            log.warn("Redis unavailable for recording failed attempt: {}", e.getMessage());
        }
    }

    public void resetFailedAttempts(String ip) {
        String key = "rate-limit:login:" + ip;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis not available for resetting failed login attempts: {}", e.getMessage());
        }
    }
}
