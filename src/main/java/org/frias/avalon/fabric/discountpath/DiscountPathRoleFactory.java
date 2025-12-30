package org.frias.avalon.fabric.discountpath;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

import java.util.List;

public interface DiscountPathRoleFactory {
    DiscountTempResult calculate(Product pentity, List<String> roles, String quantity) ;
}
