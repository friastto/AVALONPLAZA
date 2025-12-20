package org.frias.avalon.ventas.services.implementation;

import org.frias.avalon.Producto.services.interfaces.ProductoService;
import org.frias.avalon.promociones.factory.PromotionFactoryService;
import org.frias.avalon.ventas.dtos.SaleRequest;
import org.frias.avalon.ventas.dtos.SalesResponseDto;
import org.frias.avalon.ventas.repositories.SaleRepository;
import org.frias.avalon.ventas.services.interfaces.SaleService;

public class SaleServiceImpl implements SaleService {

    private final ProductoService productoService;
    private final PromotionFactoryService promotionFactoryService;
    private final SaleRepository saleRepository;

    public SaleServiceImpl(ProductoService productoService, PromotionFactoryService promotionFactoryService, SaleRepository saleRepository) {
        this.productoService = productoService;
        this.promotionFactoryService = promotionFactoryService;
        this.saleRepository = saleRepository;
    }


    @Override
    public SalesResponseDto salesProccesor(SaleRequest saleRequest) {

        saleRequest.saleDetails().forEach(product -> {









        });




        return null;
    }
}
