package org.frias.avalon.promociones.factory;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.promociones.entities.Promotion;
import org.frias.avalon.promociones.factory.interfaz.PromotionStrategy;
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
    public BigDecimal getFinalPrice(Product product, Boolean isEmployee) {

        if (product.getPromotions() == null || product.getPromotions().isEmpty()) {
        return product.getPrice();
    }

        return product.getPromotions().stream()
                .filter(Promotion::estaActiva)
                .filter(p -> {
                    String typeCode = p.getPromoTypeId().getShortName();
                    // Si el sistema validó que es empleado, buscamos su promo específica
                    if (isEmployee) return typeCode.equals("PROMO_EMPLOYEE");
                    // Si no, buscamos promociones para todo público
                    return typeCode.equals("PROMO_GLOBAL") ;//|| typeCode.equals("BLACK_FRIDAY");
                })
                .findFirst()
                .map(p -> strategies.get(p.getPromoTypeId().getShortName()).applyDiscount(product.getPrice(), p))
                .orElse(product.getPrice());
    }

}
