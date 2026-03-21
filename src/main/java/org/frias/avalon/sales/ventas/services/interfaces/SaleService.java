package org.frias.avalon.sales.ventas.services.interfaces;

import org.frias.avalon.sales.ventas.dtos.SaleRequest;
import org.frias.avalon.sales.ventas.dtos.SalesResponseDto;

public interface SaleService {


    SalesResponseDto salesProccesor(SaleRequest saleRequest);


}
