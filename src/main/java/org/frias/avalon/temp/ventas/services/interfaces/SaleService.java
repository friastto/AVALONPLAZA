package org.frias.avalon.temp.ventas.services.interfaces;

import org.frias.avalon.temp.ventas.dtos.SaleRequest;
import org.frias.avalon.temp.ventas.dtos.SalesResponseDto;

public interface SaleService {


    SalesResponseDto salesProccesor(SaleRequest saleRequest);


}
