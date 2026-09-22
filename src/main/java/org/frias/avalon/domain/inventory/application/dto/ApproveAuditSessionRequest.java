package org.frias.avalon.domain.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ApproveAuditSessionRequest(
        @NotNull(message = "El auditSessionId es obligatorio")
        Long auditSessionId,
        @NotNull(message = "El approverUserId es obligatorio")
        Long approverUserId,
        String approverName,
        String notes,
        List<AuditSignatureRequest> signatures
) {}
