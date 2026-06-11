package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;

public interface LinkBarcodeToProductUseCase {

    /**
     * Vincula un nuevo código de barras a un producto existente.
     *
     * @param request El DTO que contiene el ID del producto y el nuevo código de barras.
     */
    void execute(LinkBarcodeRequest request);
}
