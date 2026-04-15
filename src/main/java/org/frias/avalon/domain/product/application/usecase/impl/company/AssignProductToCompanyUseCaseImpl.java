package org.frias.avalon.domain.product.application.usecase.impl.company;

import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.company.AssingProductToCompanyUseCase;
import org.springframework.stereotype.Service;

@Service
public class AssignProductToCompanyUseCaseImpl extends TenantSecurity implements AssingProductToCompanyUseCase {
    private final ProductoService ps;

    public AssignProductToCompanyUseCaseImpl(ProductoService ps) {
        this.ps = ps;
    }


    @Override
    public ProductResponseDto execute(Long idProduct) {


       return null;
    }
}
