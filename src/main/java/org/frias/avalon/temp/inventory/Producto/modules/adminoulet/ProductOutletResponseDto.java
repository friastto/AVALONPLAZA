package org.frias.avalon.temp.inventory.Producto.modules.adminoulet;

import java.math.BigDecimal;
import java.util.List;

public record ProductOutletResponseDto(
        Long id,
        String displayName,      // El nombre ya resuelto (Custom o Global)
        String displayDescription,
        String imageUrl,
        String categoryName,
        String unitName,
        BigDecimal price,
        Integer stock,
        List<String> barcodes  // Lista de todos los códigos que activan este producto
            // Para que el Front sepa si el nombre es propio o global


) {
}
