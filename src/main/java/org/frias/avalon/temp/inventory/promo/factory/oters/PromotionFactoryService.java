package org.frias.avalon.temp.inventory.promo.factory.oters;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.temp.inventory.promo.dtos.DiscountTempResult;

public interface PromotionFactoryService {

    DiscountTempResult getFinalPrice(ProductOutlet productOutlet, Boolean isEmployee);
}
