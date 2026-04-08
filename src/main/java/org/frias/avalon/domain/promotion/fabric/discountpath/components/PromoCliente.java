package org.frias.avalon.domain.promotion.fabric.discountpath.components;

import org.frias.avalon.domain.promotion.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.domain.promotion.fabric.discountpath.interfaces.Strategy;
import org.frias.avalon.domain.promotion.fabric.priceCalculator.PriceCalculator;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.domain.inventory.promo.entities.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Component
public class PromoCliente implements Strategy {
    private final ConvertFactoryService convertFactoryService;

private final PriceCalculator priceCalculator;
private final String gramos= "GR";

    private static final Set<String> UNIDADES_PESABLES =
            Set.of("LT", "GR", "KG");

    public PromoCliente(ConvertFactoryService convertFactoryService, PriceCalculator priceCalculator) {
        this.convertFactoryService = convertFactoryService;
        this.priceCalculator = priceCalculator;
    }

    @Override
    public DiscountTempResult calculatePrice(ProductOutlet productOutlet, String quantity) {

        BigDecimal precioBase = productOutlet.getLocalPrice();

    BigDecimal cant = new BigDecimal(quantity);

    String medida =productOutlet.getCompanyProduct().getProduct().getUnit().getShortName();

        return productOutlet.getLocalPromotions().stream()
                .filter(Promotion::estaActiva)
                .findFirst()
                .map(promo -> {
                    // Obtenemos el tipo de promo desde tu MasterData
                    String tipoPromo = promo.getPromoTypeId().getShortName();

                    return switch (tipoPromo) {
                        case "2X1" -> calcularLlevaXPagaY(precioBase, cant, 2, 1, promo.getDescription());
                        case "3X2" -> calcularLlevaXPagaY(precioBase, cant, 3, 2, promo.getDescription()); // Nota: puse 3x2 aunque tu shortName dice 2x3, suele ser 3x2
                        case "2DAPCT" -> calcularSegundaUnidadDescuento(precioBase, cant, promo);
                        case "LLEVXPGY" -> calcularLlevaXPagaY(precioBase, cant, 10, 8, promo.getDescription()); // Ejemplo: lleva 10 paga 8

                        default -> calcularPorcentajeSimple(precioBase, quantity, promo,medida);
                    };
                })
                .orElse(precioRegular(precioBase, quantity));
    }

    private DiscountTempResult calcularLlevaXPagaY(BigDecimal precio, BigDecimal cant, int lleva, int paga, String desc) {
        // Convertimos a entero para promos de unidades.
        // Si es carne (1.2kg), estas promos no suelen aplicar, pero el sistema debe ser robusto.
        int cantidadEntera = cant.intValue();
        int gruposDePromo = cantidadEntera / lleva;
        int sobrantes = cantidadEntera % lleva;

        // Total de unidades que se cobran
        int unidadesACobrar = (gruposDePromo * paga) + sobrantes;

        BigDecimal precioFinal = precio.multiply(new BigDecimal(unidadesACobrar));
        BigDecimal ahorro = precio.multiply(new BigDecimal(cantidadEntera)).subtract(precioFinal);

        return new DiscountTempResult(ahorro, "Promo " + lleva + "x" + paga + ": " + desc, precioFinal);
    }

    private DiscountTempResult calcularSegundaUnidadDescuento(BigDecimal precio, BigDecimal cant, Promotion promo) {
        int cantidadEntera = cant.intValue();
        int parejas = cantidadEntera / 2;
        int impares = cantidadEntera % 2;

        // El discount (ej: 50%) se aplica solo a una unidad de cada pareja
        BigDecimal descuentoPorUnidad = precio.multiply(promo.getDiscount().divide(new BigDecimal("100")));
        BigDecimal ahorroTotal = descuentoPorUnidad.multiply(new BigDecimal(parejas));

        BigDecimal subtotalSinDesc = precio.multiply(cant);
        BigDecimal precioFinal = subtotalSinDesc.subtract(ahorroTotal);

        return new DiscountTempResult(ahorroTotal, "2da Unidad con -" + promo.getDiscount() + "%", precioFinal);
    }

    private DiscountTempResult calcularPorcentajeSimple(BigDecimal precioBase, String cantidad, Promotion promo,String medida) {
        // 1. Calcular el subtotal sin discount
        // Ej: 1200g * $2.3 (price por gramo) = $2760

        BigDecimal subtotalSinDescuento = precioBase.multiply(new BigDecimal(cantidad));



        if (UNIDADES_PESABLES.contains(medida)) {

            BigDecimal cant = convertFactoryService.convertTo(cantidad,medida,false);

            subtotalSinDescuento = priceCalculator.calculatePriceXWeight(precioBase,medida,cant);




        }

        // 2. Obtener el porcentaje de la promoción (ej: 10%)
        BigDecimal porcentaje = promo.getDiscount();

        // 3. Calcular el monto del ahorro: (Subtotal * Porcentaje) / 100
        BigDecimal montoAhorrado = subtotalSinDescuento.multiply(porcentaje)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // 4. Calcular el price final
        BigDecimal precioFinal = subtotalSinDescuento.subtract(montoAhorrado);

        // 5. Retornar tu Record con la descripción de tu MasterData
        return new DiscountTempResult(
                montoAhorrado,
                "Descuento de " + porcentaje + "%: " + promo.getDescription(),
                precioFinal
        );
    }

    private DiscountTempResult precioRegular(BigDecimal precio, String cant) {


        return new DiscountTempResult(BigDecimal.ZERO, "Precio Regular", precio.multiply(new BigDecimal(cant)));
    }
}
