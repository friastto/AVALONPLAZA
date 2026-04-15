package org.frias.avalon.domain.product.application.usecase.impl.company;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.company.UpdateProductCompanyUseCase;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UpdateProducCompanytUseCaseImpl extends TenantSecurity implements UpdateProductCompanyUseCase {
    private final ProductoCompanyService productoCompanyService;
    private final ProductoService productoService;

    public UpdateProducCompanytUseCaseImpl(ProductoCompanyService productoCompanyService, ProductoService productoService) {

        this.productoCompanyService = productoCompanyService;
        this.productoService = productoService;
    }


    @Override
    public ProductResponseDto execute(ProductRequestCreate dto, MultipartFile file) {

        if (!isMasterStaff()) {
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }

        return null;
    }
}
