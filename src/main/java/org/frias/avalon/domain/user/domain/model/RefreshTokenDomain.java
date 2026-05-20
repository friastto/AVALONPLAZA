package org.frias.avalon.domain.user.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class RefreshTokenDomain {

    private UUID id;
    private String token;
    private Long userAvalonId; // Identificador del usuario (ej. username)
    private Instant expiryDate;
    private boolean revoked;
    private Instant issuedAt;

    // Constructor para crear un nuevo token
    public RefreshTokenDomain(String token, Long userAvalonId, Instant expiryDate) {
        this.id = UUID.randomUUID();
        this.token = token;
        this.userAvalonId = userAvalonId;
        this.expiryDate = expiryDate;
        this.revoked = false;
        this.issuedAt = Instant.now();
    }

    // Constructor para reconstruir desde persistencia
    public RefreshTokenDomain(UUID id, String token, Long userAvalonId, Instant expiryDate, boolean revoked, Instant issuedAt) {
        this.id = id;
        this.token = token;
        this.userAvalonId = userAvalonId;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.issuedAt = issuedAt;
    }



    public boolean isRevoked() {
        return revoked;
    }

    // Setters (para modificar el estado, ej. revocar)
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    // Método de negocio para verificar expiración
    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    // Método de negocio para revocar
    public void revoke() {
        this.revoked = true;
    }
}
