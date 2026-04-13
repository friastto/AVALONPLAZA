package org.frias.avalon.domain.product.application.services.interfaces;

import org.frias.avalon.domain.product.application.dto.ProductBarcodeRequestDto;

public interface ProductoBarcodeServices {

    boolean addBarcode(ProductBarcodeRequestDto productBarcodeRequestDto);
}
