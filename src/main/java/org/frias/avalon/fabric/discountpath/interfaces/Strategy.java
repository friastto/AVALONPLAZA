package org.frias.avalon.fabric.discountpath.interfaces;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;

public interface Strategy {

    DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity);
}
