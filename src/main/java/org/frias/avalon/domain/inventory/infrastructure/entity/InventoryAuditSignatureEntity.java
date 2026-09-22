package org.frias.avalon.domain.inventory.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_audit_signature")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InventoryAuditSignatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_session_id", nullable = false)
    private InventoryAuditSessionEntity auditSession;

    @Column(name = "signer_type", nullable = false, length = 30)
    private String signerType;

    @Column(name = "signer_user_id", nullable = false)
    private Long signerUserId;

    @Column(name = "signer_name", nullable = false, length = 150)
    private String signerName;

    @Column(name = "signature_base64", nullable = false, columnDefinition = "TEXT")
    private String signatureBase64;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;
}
