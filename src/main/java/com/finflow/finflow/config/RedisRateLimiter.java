package com.finflow.finflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisRateLimiter {
    private final StringRedisTemplate redisTemplate;

    private static final long LIMIT = 10; // max requests
    private static final long WINDOW = 60; // seconds

    public boolean isAllowed(String key){
        String redisKey = "ratelimit:"+ key;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1){
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW));
        }

        return count <= LIMIT;
    }
}
