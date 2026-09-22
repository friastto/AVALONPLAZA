package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.CreateAuditSessionRequest;

public interface CreateAuditSessionUseCase {
    AuditSessionDetailResponse execute(CreateAuditSessionRequest request);
}
