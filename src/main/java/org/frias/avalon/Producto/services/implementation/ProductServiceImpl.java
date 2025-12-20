package org.frias.avalon.Producto.services.implementation;

import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;
import org.frias.avalon.Producto.services.interfaces.ProductoService;
import org.frias.avalon.promociones.factory.PromotionFactoryService;

import java.math.BigDecimal;

public class ProductServiceImpl implements ProductoService {

    private final PromotionFactoryService promoFactoryService;


    public ProductServiceImpl(PromotionFactoryService promoFactoryService) {
        this.promoFactoryService = promoFactoryService;
    }


    @Override
    public BigDecimal calculatePrice(Long productId, String identity) {



        return null;
    }

    @Override
    public ProductResponseDto save(ProductRequestCreate request) {



return null;



    }
}
