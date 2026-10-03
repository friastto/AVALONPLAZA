package org.frias.avalon.domain.subscription.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service handling cryptographic operations for Wompi Colombia integration.
 * Implements SHA-256 integrity signature generation and webhook checksum validation.
 */
@Service
public class WompiSecurityService {

    private final String integritySecret;
    private final String eventsSecret;

    public WompiSecurityService(
            @Value("${wompi.integrity-secret:prod_integrity_dummy}") String integritySecret,
            @Value("${wompi.events-secret:prod_events_dummy}") String eventsSecret
    ) {
        this.integritySecret = integritySecret;
        this.eventsSecret = eventsSecret;
    }

    /**
     * Generates a SHA-256 integrity signature for checkout session.
     * Formula: SHA-256(reference + amountInCents + currency + integritySecret)
     */
    public String generateIntegritySignature(String reference, long amountInCents, String currency) {
        String raw = reference + amountInCents + currency + integritySecret;
        return sha256Hex(raw);
    }

    /**
     * Validates that the provided checksum matches the calculated SHA-256 for a checkout session.
     */
    public boolean validateIntegritySignature(String reference, long amountInCents, String currency, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String expected = generateIntegritySignature(reference, amountInCents, currency);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Validates a Wompi webhook event signature using the events secret.
     * Formula: SHA-256(concatenated property values + timestamp + eventsSecret)
     */
    public boolean validateWebhookSignature(String concatenatedValues, String timestamp, String receivedChecksum) {
        if (receivedChecksum == null || receivedChecksum.isBlank()) {
            return false;
        }
        String raw = (concatenatedValues != null ? concatenatedValues : "") + (timestamp != null ? timestamp : "") + eventsSecret;
        String expected = sha256Hex(raw);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedChecksum.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in JVM", e);
        }
    }
}
