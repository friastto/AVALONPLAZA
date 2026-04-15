package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.saas.GetAllProductsUseCase;
import org.frias.avalon.domain.product.application.usecase.saas.UpdateProductUseCase;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class GetAllProductsUseCaseImpl extends TenantSecurity implements GetAllProductsUseCase {
   private final ProductoService ps;
    private final ProductoMapperService productoMapperService;

    public GetAllProductsUseCaseImpl(ProductoService ps, ProductoMapperService productoMapperService) {
        this.ps = ps;
        this.productoMapperService = productoMapperService;
    }


    @Override
    public List<ProductAvalonResponseDto> execute() {

        if(!isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }
List<Product> productList = ps.getAllProducts();

        return productList.stream()
                .map(productoMapperService::toDto)
                .toList();
    }
}
