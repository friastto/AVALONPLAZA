package org.frias.avalon.domain.user.application.dtos.response;

import java.io.Serializable;

/**
 * Real-time event payload broadcasted via WebSocket to notify active clients
 * of user session changes (employee status inactivation/reactivation, transfer, etc).
 */
public record SessionSyncMessage(
        String eventType,
        Long userId,
        String statusCode,
        Long outletId,
        Long timestamp
) implements Serializable {
}
