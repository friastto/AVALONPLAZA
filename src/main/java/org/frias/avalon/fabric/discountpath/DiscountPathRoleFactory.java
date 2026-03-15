package org.frias.avalon.fabric.discountpath;

import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountPathRoleFactory {
    DiscountTempResult calculate(ProductOutlet productOutlet, List<String> roles, String quantity) ;
}
