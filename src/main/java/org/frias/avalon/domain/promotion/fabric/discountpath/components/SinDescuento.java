package org.frias.avalon.domain.promotion.fabric.discountpath.components;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.domain.promotion.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.domain.promotion.fabric.discountpath.interfaces.Strategy;
import org.frias.avalon.domain.promotion.fabric.priceCalculator.PriceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class SinDescuento implements Strategy {

    private final ConvertFactoryService convertFactoryService;

    private final PriceCalculator priceCalculator;
    @Autowired
    private PromoCliente promoCliente;
    private static final Set<String> UNIDADES_PESABLES =
            Set.of("LT", "GR", "KG");

    public SinDescuento(ConvertFactoryService convertFactoryService, PriceCalculator priceCalculator) {
        this.convertFactoryService = convertFactoryService;
        this.priceCalculator = priceCalculator;
    }

    @Override
    public DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity) {

        BigDecimal precioBase = productOutlet.getLocalPrice();

        BigDecimal subtotalSinDescuento = precioBase.multiply(new BigDecimal(quantity));

        String unit = productOutlet.getCompanyProduct().getProduct().getUnit().getShortName();


        if (UNIDADES_PESABLES.contains(unit)) {

            BigDecimal cant = convertFactoryService.convertTo(quantity,unit,false);

            subtotalSinDescuento = priceCalculator.calculatePriceXWeight(precioBase,unit,cant);
        }

        // 5. Retornar tu Record con la descripción de tu MasterData
        return new DiscountTempResult(
                subtotalSinDescuento,
                "SIN DESCUENTO",
                subtotalSinDescuento
        );
    }
}
