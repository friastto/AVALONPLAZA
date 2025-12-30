package org.frias.avalon.fabric.discountpath;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.fabric.discountpath.components.DescuentoEmpleado;
import org.frias.avalon.fabric.discountpath.components.PromoCliente;
import org.frias.avalon.fabric.discountpath.components.SinDescuento;
import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscountPathRoleFactoryImpl implements DiscountPathRoleFactory {

    private final DescuentoEmpleado descuentoEmpleado;
    private final PromoCliente promoCliente;
    private final SinDescuento sinDescuento;


    public DiscountPathRoleFactoryImpl(DescuentoEmpleado descuentoEmpleado, PromoCliente promoCliente, ConvertFactoryService convertFactoryService, SinDescuento sinDescuento) {
        this.descuentoEmpleado = descuentoEmpleado;
        this.promoCliente = promoCliente;

        this.sinDescuento = sinDescuento;
    }


    @Override
    public DiscountTempResult calculate(Product p, List<String> roles, String quantity) {

        if(roles.isEmpty()) return sinDescuento.calculatePrice(p,quantity);

        boolean isEmployee = roles.stream().anyMatch(r ->
                List.of("DIREC", "GERENTE", "ADMIN","CAJERO").contains(r));

        if (isEmployee) {
            return descuentoEmpleado.calculatePrice(p,quantity);
        }

        return promoCliente.calculatePrice(p,quantity);

    }
}
