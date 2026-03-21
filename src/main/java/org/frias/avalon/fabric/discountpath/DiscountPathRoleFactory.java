package org.frias.avalon.fabric.discountpath;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;

import java.util.List;

public interface DiscountPathRoleFactory {
    DiscountTempResult calculate(ProductOutlet productOutlet, List<String> roles, String quantity) ;
}
