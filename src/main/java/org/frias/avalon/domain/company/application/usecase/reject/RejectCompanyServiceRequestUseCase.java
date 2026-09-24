package org.frias.avalon.domain.company.application.usecase.reject;

import org.frias.avalon.domain.company.application.dto.request.RejectCompanyServiceRequestDto;

public interface RejectCompanyServiceRequestUseCase {
    void execute(Long companyId, RejectCompanyServiceRequestDto request);
}
