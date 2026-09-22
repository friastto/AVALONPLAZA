package org.frias.avalon.domain.inventory.application.dto;

public record AuditSignatureDto(
        Long id,
        String signerType,
        Long signerUserId,
        String signerName,
        String signatureBase64,
        String signedAt
) {}
