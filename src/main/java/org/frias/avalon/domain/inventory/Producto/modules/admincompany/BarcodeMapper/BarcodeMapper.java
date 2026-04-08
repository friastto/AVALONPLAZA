package org.frias.avalon.domain.inventory.Producto.modules.admincompany.BarcodeMapper;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarcodeMapper {



        public ProductOutletResponseDto toResponseDto(ProductOutlet outletItem) {
            Product globalProduct = outletItem.getCompanyProduct().getProduct();

            // 1. Resolvemos el name (Prioridad: Custom > Global)
            String finalName = Optional.ofNullable(outletItem.getLocalName())
                    .filter(name -> !name.isBlank())
                    .orElse(globalProduct.getName());

            // 2. Resolvemos la descripción
            String finalDesc = Optional.ofNullable(outletItem.getLocalDescription())
                    .filter(desc -> !desc.isBlank())
                    .orElse(globalProduct.getDescription());

            // 3. Recopilamos los códigos de barras (Globales + los de esta empresa)
            List<String> allBarcodes = globalProduct.getBarcodes().stream()
                    .filter(b -> b.getCompany() == null || b.getCompany().equals(outletItem.getCompanyProduct().getCompany()))
                    .map(ProductBarcode::getBarcode)
                    .toList();

            return new ProductOutletResponseDto(
                    outletItem.getId(),
                    finalName,
                    finalDesc,
                    Optional.ofNullable(outletItem.getLocalImageUrl()).orElse(globalProduct.getImageUrl()),
                    globalProduct.getCategory().getShortName(),
                    globalProduct.getUnit().getShortName(),
                    outletItem.getLocalPrice(),
                    outletItem.getStock(),
                    allBarcodes

            );
        }
    }






