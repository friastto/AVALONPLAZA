package org.frias.avalon.ventas.mappers;

import org.frias.avalon.Producto.dtos.ProductResponseDetailDto;
import org.frias.avalon.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.ventas.dtos.SaleDetailResponseDto;
import org.frias.avalon.ventas.dtos.SalesResponseDto;
import org.frias.avalon.ventas.entities.Sale;
import org.frias.avalon.ventas.entities.SaleDetail;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalesMapperService {

    private final ConvertFactoryService convertFactoryService;

    private final Set<String> unitMasaPesable = Set.of("KG", "LB", "GR");


    public SalesMapperService(ConvertFactoryService convertFactoryService) {
        this.convertFactoryService = convertFactoryService;
    }


    public SalesResponseDto toResponseDto(Sale sale) {
        return new SalesResponseDto(
                sale.getId(),
                sale.getSaleCode(), // UUID generado en la persistencia
                sale.getTotal(),
                sale.getAmountReceived(),
                sale.getAmountReturned(),
                sale.getPaymentMethodId().getFullName(),
                // Obtenemos el nombre completo desde la relación User -> Person
                sale.getEnployeeId().getPerson().getName() + " " + sale.getEnployeeId().getPerson().getLastName(),
                // Identificación del cliente (Cédula)
                sale.getCustomerId().getNumberid(),
                sale.getStatusId().getFullName(),
                // Mapeamos la lista de detalles
                sale.getDetails().stream()
                        .map(this::mapToDetailResponseDto)
                        .collect(Collectors.toList())
        );
    }

    private SaleDetailResponseDto mapToDetailResponseDto(SaleDetail detail) {

        String quantity = detail.getQuantity().toString();

        if (unitMasaPesable.contains(detail.getProduct().getUnit().getShortName()))
            quantity = convertFactoryService.convertTo(
                    detail.getQuantity().toString()
                    , detail.getProduct().getUnit().getShortName()
                    , true
            ).toString();


        return new SaleDetailResponseDto(
                detail.getId(),
                quantity,
                detail.getUnitPrice(),
                detail.getSubTotal(), // El subtotal ya con descuentos aplicados
                new ProductResponseDetailDto(
                        detail.getProduct().getId(),
                        detail.getProduct().getSku(),
                        detail.getProduct().getName(),
                        detail.getProduct().getDescription(),
                        detail.getProduct().getUnit().getShortName()
                        , detail.getProduct().getPrice()

                )
        );
    }


}
