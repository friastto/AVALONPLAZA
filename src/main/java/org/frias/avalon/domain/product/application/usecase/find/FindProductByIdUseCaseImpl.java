package org.frias.avalon.domain.product.application.usecase.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProductByIdUseCaseImpl implements FindProductByIdUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final ProductOutletMapper productOutletMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse execute(Long productId) {
        // 1. Obtener el producto usando el ID
        ProductDomain productDomain = productOutletRepositoryPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + productId + " no fue encontrado en la base de datos."));

        // 2. Obtener el código de barras asociado al producto
        String barcode = barcodeRepositoryPort.findByProductOutlet(productId).stream()
                .map(BarcodeDomain::getBarcode)
                .findFirst()
                .orElse(null);

        // 3. Mapear y devolver el resultado con el código de barras
        return productOutletMapper.toResponse(productDomain, barcode);
    }
}
