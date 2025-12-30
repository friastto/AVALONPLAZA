package org.frias.avalon.fabric.discountpath.components;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.fabric.discountpath.interfaces.Strategy;
import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescuentoEmpleado implements Strategy {
    @Autowired
    private PromoCliente promoCliente;

    @Override
    public DiscountTempResult calculatePrice(Product p, String quantity) {

        BigDecimal precioBase = p.getPrice();
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
