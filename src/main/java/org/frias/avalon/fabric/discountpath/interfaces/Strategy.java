package org.frias.avalon.fabric.discountpath.interfaces;

import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

import java.math.BigDecimal;

public interface Strategy {

    DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity);
}
