package org.frias.avalon.domain.user.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Pure Java Domain model representing a Refresh Token in ApiAvalon.
 * Free of Lombok annotations.
 */
public class RefreshTokenDomain {

    private UUID id;
    private String refreshToken;
    private Long userAvalonId;
    private Instant expiryDate;
    private boolean revoked;
    private Instant issuedAt;

    public RefreshTokenDomain(String refreshToken, Long userAvalonId, Instant expiryDate) {
        this.id = UUID.randomUUID();
        this.refreshToken = refreshToken;
        this.userAvalonId = userAvalonId;
        this.expiryDate = expiryDate;
        this.revoked = false;
        this.issuedAt = Instant.now();
    }

    public RefreshTokenDomain(UUID id, String refreshToken, Long userAvalonId, Instant expiryDate, boolean revoked, Instant issuedAt) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.userAvalonId = userAvalonId;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.issuedAt = issuedAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() { return id; }
    public String getRefreshToken() { return refreshToken; }
    public Long getUserAvalonId() { return userAvalonId; }
    public Instant getExpiryDate() { return expiryDate; }
    public Instant getIssuedAt() { return issuedAt; }
}
