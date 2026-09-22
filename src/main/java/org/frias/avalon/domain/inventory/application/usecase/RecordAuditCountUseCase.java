package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.AuditItemCountRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditItemDto;

public interface RecordAuditCountUseCase {
    AuditItemDto execute(AuditItemCountRequest request);
}
