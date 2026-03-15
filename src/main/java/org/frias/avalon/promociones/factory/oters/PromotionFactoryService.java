package org.frias.avalon.promociones.factory.oters;

import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

public interface PromotionFactoryService {

    DiscountTempResult getFinalPrice(ProductOutlet productOutlet, Boolean isEmployee);
}
