package org.frias.avalon.ventas.services.interfaces;

import org.frias.avalon.ventas.dtos.SaleRequest;
import org.frias.avalon.ventas.dtos.SalesResponseDto;

public interface SaleService {


    SalesResponseDto salesProccesor(SaleRequest saleRequest);


}
