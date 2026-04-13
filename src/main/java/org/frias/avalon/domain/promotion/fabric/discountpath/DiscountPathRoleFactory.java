package org.frias.avalon.domain.promotion.fabric.discountpath;

import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;

import java.util.List;

public interface DiscountPathRoleFactory {
    DiscountTempResult calculate(ProductOutlet productOutlet, List<String> roles, String quantity) ;
}
