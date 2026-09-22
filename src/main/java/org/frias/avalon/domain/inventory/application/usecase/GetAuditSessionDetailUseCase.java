package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;

public interface GetAuditSessionDetailUseCase {
    AuditSessionDetailResponse findById(Long auditSessionId);
    AuditSessionDetailResponse findActiveByOutletId(Long outletId);
}
