package org.frias.avalon.fabric.discountpath.interfaces;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.promociones.dtos.DiscountTempResult;

public interface Strategy {

    DiscountTempResult calculatePrice(Product p, String quantity);
}
