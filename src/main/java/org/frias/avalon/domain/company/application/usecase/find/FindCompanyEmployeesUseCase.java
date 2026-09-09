package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;

import java.util.List;

public interface FindCompanyEmployeesUseCase {
    List<CompanyEmployeeResponse> execute(Long companyId);
}
