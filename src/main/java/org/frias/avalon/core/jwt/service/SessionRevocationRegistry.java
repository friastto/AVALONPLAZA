package org.frias.avalon.core.jwt.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe registry tracking session revocations per user.
 * Allows O(1) instantaneous invalidation of JWT tokens issued prior to an
 * employee status change, role revocation, or administrative suspension.
 */
@Component
public class SessionRevocationRegistry {

    private final ConcurrentHashMap<Long, Long> revocations = new ConcurrentHashMap<>();

    /**
     * Marks all active tokens issued to the given user prior to now as revoked.
     *
     * @param userId ID of the user whose session is being revoked.
     */
    public void revokeUser(Long userId) {
        if (userId != null) {
            revocations.put(userId, Instant.now().getEpochSecond());
        }
    }

    /**
     * Verifies if a token issued at the specified timestamp has been revoked.
     *
     * @param userId   User ID from token claims.
     * @param issuedAt Token issuance instant.
     * @return true if token was issued prior to user's revocation instant.
     */
    public boolean isRevoked(Long userId, Instant issuedAt) {
        if (userId == null || issuedAt == null) {
            return false;
        }
        Long revocationEpoch = revocations.get(userId);
        if (revocationEpoch == null) {
            return false;
        }
        return issuedAt.getEpochSecond() <= revocationEpoch;
    }

    /**
     * Clears revocation record for a user (e.g. after fresh login).
     */
    public void clearRevocation(Long userId) {
        if (userId != null) {
            revocations.remove(userId);
        }
    }
}
