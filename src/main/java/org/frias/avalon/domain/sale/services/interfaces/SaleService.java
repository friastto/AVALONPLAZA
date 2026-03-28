package org.frias.avalon.domain.sale.services.interfaces;

import org.frias.avalon.domain.sale.dtos.SaleRequest;
import org.frias.avalon.domain.sale.dtos.SalesResponseDto;

public interface SaleService {


    SalesResponseDto salesProccesor(SaleRequest saleRequest);


}
