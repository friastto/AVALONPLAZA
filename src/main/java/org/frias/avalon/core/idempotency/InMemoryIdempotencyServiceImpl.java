package org.frias.avalon.core.idempotency;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryIdempotencyServiceImpl implements IdempotencyService {

    private record CachedItem(String status, String responseJson, Instant expiresAt) {}

    private final Map<String, CachedItem> cache = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean tryLock(String key, long ttlSeconds) {
        cleanExpired();
        CachedItem existing = cache.get(key);
        if (existing != null && existing.expiresAt().isAfter(Instant.now())) {
            return false;
        }
        // Registrar como IN_PROGRESS con tiempo límite de 30 segundos para liberar si el hilo muere
        cache.put(key, new CachedItem("IN_PROGRESS", null, Instant.now().plusSeconds(Math.min(ttlSeconds, 30))));
        return true;
    }

    @Override
    public boolean isCompleted(String key) {
        CachedItem item = cache.get(key);
        return item != null && "COMPLETED".equals(item.status()) && item.expiresAt().isAfter(Instant.now());
    }

    @Override
    public String getStoredResponse(String key) {
        CachedItem item = cache.get(key);
        return item != null ? item.responseJson() : null;
    }

    @Override
    public void saveResponse(String key, String jsonResponse, long ttlSeconds) {
        cache.put(key, new CachedItem("COMPLETED", jsonResponse, Instant.now().plusSeconds(ttlSeconds)));
    }

    @Override
    public void removeKey(String key) {
        cache.remove(key);
    }

    private void cleanExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
