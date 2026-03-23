package org.frias.avalon.temp.fabric.discountpath;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.temp.inventory.promo.dtos.DiscountTempResult;

import java.util.List;

public interface DiscountPathRoleFactory {
    DiscountTempResult calculate(ProductOutlet productOutlet, List<String> roles, String quantity) ;
}
