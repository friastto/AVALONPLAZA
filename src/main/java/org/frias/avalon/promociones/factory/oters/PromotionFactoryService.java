package org.frias.avalon.promociones.factory.oters;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

public interface PromotionFactoryService {

    DiscountTempResult getFinalPrice(Product product, Boolean isEmployee);
}
