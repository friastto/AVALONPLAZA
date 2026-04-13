package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.saas.UpdateProductUseCase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UpdateProductUseCaseImpl extends TenantSecurity implements UpdateProductUseCase {
   private final ProductoService ps;

    public UpdateProductUseCaseImpl(ProductoService ps) {
        this.ps = ps;
    }


    @Override
    public ProductAvalonResponseDto execute(ProductRequestCreate dto, MultipartFile file) {

        if(!isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }

        return null; // ps.save(dto,file);
    }
}
