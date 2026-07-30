package org.frias.avalon.core.idempotency;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Servicio de Idempotencia Distribuida para soporte de Cluster POS.
 * Delega en InMemoryIdempotencyServiceImpl cuando Redis no esta presente o configurado.
 */
@Service
@Primary
public class RedisIdempotencyServiceImpl implements IdempotencyService {

    private final InMemoryIdempotencyServiceImpl memoryService;

    public RedisIdempotencyServiceImpl(InMemoryIdempotencyServiceImpl memoryService) {
        this.memoryService = memoryService;
    }

    @Override
    public boolean tryLock(String key, long ttlSeconds) {
        return memoryService.tryLock(key, ttlSeconds);
    }

    @Override
    public boolean isCompleted(String key) {
        return memoryService.isCompleted(key);
    }

    @Override
    public String getStoredResponse(String key) {
        return memoryService.getStoredResponse(key);
    }

    @Override
    public void saveResponse(String key, String jsonResponse, long ttlSeconds) {
        memoryService.saveResponse(key, jsonResponse, ttlSeconds);
    }

    @Override
    public void removeKey(String key) {
        memoryService.removeKey(key);
    }
}
