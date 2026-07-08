package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkBarcodeToProductUseCaseImpl implements LinkBarcodeToProductUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final BarcodeRepositoryPort barcodeRepositoryPort;

    @Override
    @Transactional
    public void execute(LinkBarcodeRequest request) {
        // 1. Crear el objeto de dominio del código de barras primero para disparar las validaciones.
        BarcodeDomain newBarcode;
        try {
            newBarcode = BarcodeDomain.create(
                    request.barcode(),
                    request.productId(),
                    request.description()
            );
        } catch (DomainValidationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // 2. Validar que el producto al que se le asignará el código de barras existe.
        productOutletRepositoryPort.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.productId()));

        // 3. Persistir el nuevo código de barras a través del puerto.
        barcodeRepositoryPort.save(newBarcode);
    }
}
