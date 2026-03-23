package org.frias.avalon.temp.fabric.discountpath.interfaces;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.temp.inventory.promo.dtos.DiscountTempResult;

public interface Strategy {

    DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity);
}
