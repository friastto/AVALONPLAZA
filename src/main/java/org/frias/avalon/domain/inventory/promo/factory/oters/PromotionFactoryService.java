package org.frias.avalon.domain.inventory.promo.factory.oters;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;

public interface PromotionFactoryService {

    DiscountTempResult getFinalPrice(ProductOutlet productOutlet, Boolean isEmployee);
}
