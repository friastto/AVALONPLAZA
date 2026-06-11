package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import lombok.RequiredArgsConstructor;
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
        // 1. Validar que el producto al que se le asignará el código de barras existe.
        productOutletRepositoryPort.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.productId()));

        // 2. Crear el objeto de dominio del código de barras.
        // La validación del código de barras (que no sea nulo/vacío) se delega al dominio.
        BarcodeDomain newBarcode = BarcodeDomain.create(
                request.barcode(),
                request.productId(),
                request.description()
        );

        // 3. Persistir el nuevo código de barras a través del puerto.
        barcodeRepositoryPort.save(newBarcode);
    }
}
