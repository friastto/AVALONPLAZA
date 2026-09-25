package org.frias.avalon.core.jwt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for SessionRevocationRegistry")
class SessionRevocationRegistryTest {

    private SessionRevocationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SessionRevocationRegistry();
    }

    @Test
    @DisplayName("Should return false when user is not revoked")
    void shouldReturnFalseWhenUserNotRevoked() {
        boolean revoked = registry.isRevoked(100L, Instant.now());
        assertFalse(revoked);
    }

    @Test
    @DisplayName("Should return true when token was issued before or at revocation time")
    void shouldReturnTrueWhenTokenIssuedBeforeRevocation() {
        Instant tokenIssuedAt = Instant.now().minusSeconds(10);
        registry.revokeUser(100L);

        boolean revoked = registry.isRevoked(100L, tokenIssuedAt);
        assertTrue(revoked);
    }

    @Test
    @DisplayName("Should return false when token was issued after revocation time")
    void shouldReturnFalseWhenTokenIssuedAfterRevocation() {
        registry.revokeUser(100L);
        Instant newTokenIssuedAt = Instant.now().plusSeconds(10);

        boolean revoked = registry.isRevoked(100L, newTokenIssuedAt);
        assertFalse(revoked);
    }

    @Test
    @DisplayName("Should return false after clearing revocation")
    void shouldReturnFalseAfterClearingRevocation() {
        Instant tokenIssuedAt = Instant.now().minusSeconds(5);
        registry.revokeUser(100L);
        assertTrue(registry.isRevoked(100L, tokenIssuedAt));

        registry.clearRevocation(100L);
        assertFalse(registry.isRevoked(100L, tokenIssuedAt));
    }

    @Test
    @DisplayName("Should handle null arguments gracefully")
    void shouldHandleNullArgumentsGracefully() {
        assertFalse(registry.isRevoked(null, Instant.now()));
        assertFalse(registry.isRevoked(100L, null));
        assertDoesNotThrow(() -> registry.revokeUser(null));
        assertDoesNotThrow(() -> registry.clearRevocation(null));
    }
}
