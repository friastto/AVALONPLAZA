package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.ApproveAuditSessionRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;

public interface ApproveAuditSessionUseCase {
    AuditSessionDetailResponse execute(ApproveAuditSessionRequest request);
}
