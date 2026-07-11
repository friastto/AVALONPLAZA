package org.frias.avalon.domain.product.application.usecase.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProductByBarcodeUseCaseImpl implements FindProductByBarcodeUseCase {

    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse execute(String barcode) {
        // 1. Buscar el código de barras en la base de datos
        BarcodeDomain barcodeDomain = barcodeRepositoryPort.findByCode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún producto asociado al código de barras: " + barcode));

        // 2. Obtener el producto usando el ID vinculado al código de barras
        ProductDomain productDomain = productOutletRepositoryPort.findById(barcodeDomain.getProductOutletId())
                .orElseThrow(() -> new ResourceNotFoundException("El producto con codigo #" + barcode + " no fue encontrado en la base de datos. (Inconsistencia de datos)"));

        // 3. Mapear y devolver el resultado con el código de barras
        return productOutletMapper.toResponse(productDomain, barcodeDomain.getBarcode());
    }
}
