package org.frias.avalon.fabric.discountpath.components;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.fabric.discountpath.interfaces.Strategy;
import org.frias.avalon.fabric.priceCalculator.PriceCalculator;
import org.frias.avalon.promociones.dtos.DiscountTempResult;
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
    public DiscountTempResult calculatePrice(Product p, String quantity) {
            BigDecimal priceBruto = p.getPrice();

        BigDecimal subtotalSinDescuento = priceBruto.multiply(new BigDecimal(quantity));



        if (UNIDADES_PESABLES.contains(p.getUnit().getShortName())) {

            BigDecimal cant = convertFactoryService.convertTo(quantity,p.getUnit().getShortName(),false);

            subtotalSinDescuento = priceCalculator.calculatePriceXWeight(priceBruto,p.getUnit().getShortName(),cant);
        }

        // 5. Retornar tu Record con la descripción de tu MasterData
        return new DiscountTempResult(
                subtotalSinDescuento,
                "SIN DESCUENTO",
                subtotalSinDescuento
        );
    }
}
