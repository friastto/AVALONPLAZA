package org.frias.avalon.domain.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuditSignatureRequest(
        @NotBlank(message = "El signerType es obligatorio")
        String signerType,
        @NotNull(message = "El signerUserId es obligatorio")
        Long signerUserId,
        @NotBlank(message = "El signerName es obligatorio")
        String signerName,
        @NotBlank(message = "La signatureBase64 es obligatoria")
        String signatureBase64
) {}
