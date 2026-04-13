package org.frias.avalon.domain.promotion.fabric.discountpath.components;

import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.domain.promotion.fabric.discountpath.interfaces.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescuentoEmpleado implements Strategy {
    @Autowired
    private PromoCliente promoCliente;

    @Override
    public DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity) {

        BigDecimal precioBase = productOutlet.getLocalPrice();

        BigDecimal porcentajeEmp = new BigDecimal("15.00"); // 15% fijo

        BigDecimal montoDescuento = precioBase.multiply(porcentajeEmp.divide(new BigDecimal("100")));
        BigDecimal precioFinal = precioBase.subtract(montoDescuento);

        return new DiscountTempResult(
                montoDescuento,
                "DESCUENTO EMPLEADO",
                precioFinal
        );
    }
}
