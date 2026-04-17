package org.frias.avalon.domain.product.application.usecase.impl.company;

import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.company.AssingProductToCompanyUseCase;
import org.frias.avalon.domain.product.domain.entity.ProductCompany;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignProductToCompanyUseCaseImpl extends TenantSecurity implements AssingProductToCompanyUseCase {
    private final ProductoService productoService;
    private final ProductoCompanyService productoCompanyService;

    public AssignProductToCompanyUseCaseImpl(ProductoService productoService, ProductoCompanyService productoCompanyService) {
        this.productoService = productoService;
        this.productoCompanyService = productoCompanyService;
    }


    @Override
    @Transactional
    public ProductResponseDto execute(Long idProduct) {

        Long idCompany = getCompanyId();

        if (idCompany == null) throw new SecurityException("no esta autorizado para asignar poductos de avalon a la empresa");

    ProductCompany productCompany = productoCompanyService.assingProductCatalogAvalonToCatalogCompany(idProduct);


       return null;
    }
}
