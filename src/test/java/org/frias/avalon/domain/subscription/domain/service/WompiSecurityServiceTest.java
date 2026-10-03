package org.frias.avalon.domain.subscription.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for WompiSecurityService")
class WompiSecurityServiceTest {

    private WompiSecurityService securityService;
    private static final String INTEGRITY_SECRET = "test_integrity_secret_123";
    private static final String EVENTS_SECRET = "test_events_secret_456";

    @BeforeEach
    void setUp() {
        securityService = new WompiSecurityService(INTEGRITY_SECRET, EVENTS_SECRET);
    }

    @Test
    @DisplayName("Should generate deterministic SHA-256 integrity signature")
    void shouldGenerateIntegritySignatureCorrectly() {
        String reference = "OUTLET-SUB-4-123456";
        long amountInCents = 15000000L;
        String currency = "COP";

        String signature = securityService.generateIntegritySignature(reference, amountInCents, currency);

        assertNotNull(signature);
        assertEquals(64, signature.length(), "SHA-256 hex string must be 64 characters");

        // Validating the same signature matches
        assertTrue(securityService.validateIntegritySignature(reference, amountInCents, currency, signature));
    }

    @Test
    @DisplayName("Should reject invalid integrity signature")
    void shouldRejectInvalidIntegritySignature() {
        String reference = "OUTLET-SUB-4-123456";
        long amountInCents = 15000000L;
        String currency = "COP";

        assertFalse(securityService.validateIntegritySignature(reference, amountInCents, currency, "invalid_signature"));
        assertFalse(securityService.validateIntegritySignature(reference, amountInCents, currency, null));
        assertFalse(securityService.validateIntegritySignature(reference, amountInCents, currency, ""));
    }

    @Test
    @DisplayName("Should validate webhook signature successfully")
    void shouldValidateWebhookSignatureSuccessfully() {
        String concatenatedValues = "tx-1234515000000COPAPPROVED";
        String timestamp = "1711670400";

        // Expected raw: concatenatedValues + timestamp + eventsSecret
        String expectedHash = securityService.sha256Hex(concatenatedValues + timestamp + EVENTS_SECRET);

        assertTrue(securityService.validateWebhookSignature(concatenatedValues, timestamp, expectedHash));
        assertFalse(securityService.validateWebhookSignature(concatenatedValues, timestamp, "wrong_hash"));
        assertFalse(securityService.validateWebhookSignature(concatenatedValues, timestamp, null));
    }
}
