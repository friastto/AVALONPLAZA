package org.frias.avalon.temp.inventory.promo.factory.oters;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.temp.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.temp.inventory.promo.entities.Promotion;
import org.frias.avalon.temp.inventory.promo.factory.oters.interfaz.PromotionStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromotionFactoryImpl implements PromotionFactoryService {

    //spring rellena automaticamente con las estrategias de descuento
    private final Map<String, PromotionStrategy> strategies;

    public PromotionFactoryImpl(List<PromotionStrategy> strategyList) {

        // Convertimos la lista de estrategias en un mapa para acceso rápido
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PromotionStrategy::getPromotionType, s -> s));
    }


    @Override
    public DiscountTempResult getFinalPrice(ProductOutlet productOutlet, Boolean isEmployee) {

        if (productOutlet.getPromotions() == null || productOutlet.getPromotions().isEmpty()) {
        return new DiscountTempResult(
                BigDecimal.ZERO,
                "No promotions found",
                productOutlet.getLocalPrice());
    }

        return productOutlet.getPromotions().stream()
                .filter(Promotion::estaActiva)
                .filter(p -> {
                    String typeCode = p.getPromoTypeId().getShortName();
                    // Si el sistema validó que es empleado, buscamos su promo específica
                    if (isEmployee) return typeCode.equals("PROMO_EMPLOYEE");
                    // Si no, buscamos promociones para todo público
                    return typeCode.equals("PROMO_GLOBAL") ;//|| typeCode.equals("BLACK_FRIDAY");
                })
                .findFirst()
                .map(p -> strategies.get(p.getPromoTypeId().getShortName()).applyDiscount(productOutlet.getLocalPrice(), p))
                .orElse(new DiscountTempResult(
                        BigDecimal.ZERO,
                        "No promotions found",
                        productOutlet.getLocalPrice()));
    }

}
