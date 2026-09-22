package org.frias.avalon.domain.inventory.application.dto;

public record AuditWebSocketMessage(
        String eventType,
        Long auditSessionId,
        Long outletId,
        AuditSessionDetailResponse session,
        AuditItemDto updatedItem
) {}
