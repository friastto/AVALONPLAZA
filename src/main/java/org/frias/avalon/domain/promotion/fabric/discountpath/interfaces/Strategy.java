package org.frias.avalon.domain.promotion.fabric.discountpath.interfaces;

import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;

public interface Strategy {

    DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity);
}
