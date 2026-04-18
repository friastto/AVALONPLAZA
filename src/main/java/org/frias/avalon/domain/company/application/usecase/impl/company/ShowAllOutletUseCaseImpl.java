package org.frias.avalon.domain.company.application.usecase.impl.company;

import org.frias.avalon.domain.company.application.usecase.inter.company.ShowAllOutletsUseCase;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowAllOutletUseCaseImpl extends TenantSecurity implements ShowAllOutletsUseCase {
    @Override
    public List<OutletDto> execute() {

        Long idCompany = getValidatedCompanyId();

        



        return List.of();
    }
}
