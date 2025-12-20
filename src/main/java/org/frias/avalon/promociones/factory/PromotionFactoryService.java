package org.frias.avalon.promociones.factory;

import org.frias.avalon.Producto.entities.Product;

import java.math.BigDecimal;

public interface PromotionFactoryService {

    BigDecimal getFinalPrice(Product product, Boolean isEmployee);
}
