package org.frias.avalon.core.idempotency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Primary
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
public class RedisIdempotencyServiceImpl implements IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final InMemoryIdempotencyServiceImpl fallbackService;

    public RedisIdempotencyServiceImpl(StringRedisTemplate redisTemplate, InMemoryIdempotencyServiceImpl fallbackService) {
        this.redisTemplate = redisTemplate;
        this.fallbackService = fallbackService;
    }

    @Override
    public boolean tryLock(String key, long ttlSeconds) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent("idempotency:lock:" + key, "IN_PROGRESS", Duration.ofSeconds(Math.min(ttlSeconds, 30)));
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            return fallbackService.tryLock(key, ttlSeconds);
        }
    }

    @Override
    public boolean isCompleted(String key) {
        try {
            String value = redisTemplate.opsForValue().get("idempotency:response:" + key);
            return value != null;
        } catch (Exception e) {
            return fallbackService.isCompleted(key);
        }
    }

    @Override
    public String getStoredResponse(String key) {
        try {
            return redisTemplate.opsForValue().get("idempotency:response:" + key);
        } catch (Exception e) {
            return fallbackService.getStoredResponse(key);
        }
    }

    @Override
    public void saveResponse(String key, String jsonResponse, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set("idempotency:response:" + key, jsonResponse, Duration.ofSeconds(ttlSeconds));
            redisTemplate.delete("idempotency:lock:" + key);
        } catch (Exception e) {
            fallbackService.saveResponse(key, jsonResponse, ttlSeconds);
        }
    }

    @Override
    public void removeKey(String key) {
        try {
            redisTemplate.delete("idempotency:lock:" + key);
            redisTemplate.delete("idempotency:response:" + key);
        } catch (Exception e) {
            fallbackService.removeKey(key);
        }
    }
}
