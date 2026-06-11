package org.frias.avalon.domain.product.application.usecase.create;

import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

public interface CreateProductOutletUseCase {

    ProductResponse execute(ProductNewDataRequest request);


}
