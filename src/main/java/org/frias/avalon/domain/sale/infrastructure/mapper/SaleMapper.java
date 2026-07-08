package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SaleMapper {

    public SaleEntity toEntity(SaleDomain domain) {
        if (domain == null) return null;

        SaleEntity entity = SaleEntity.builder()
                .id(domain.getId())
                .saleCode(domain.getSaleCode())
                .totalAmount(domain.getTotalAmount())
                .amountReceived(domain.getAmountReceived())
                .changeGiven(domain.getChangeGiven())
                .paymentMethodId(domain.getPaymentMethodId())
                .statusId(domain.getStatusId())
                .clientId(domain.getClientId())
                .outletId(domain.getOutletId())
                .employeeId(domain.getEmployeeId())
                .saleDate(domain.getSaleDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getItems() != null) {
            List<SaleItemEntity> itemEntities = domain.getItems().stream()
                    .map(item -> toItemEntity(item, entity))
                    .collect(Collectors.toList());
            entity.setItems(itemEntities);
        }

        return entity;
    }

    public SaleItemEntity toItemEntity(SaleItemDomain domain, SaleEntity saleEntity) {
        if (domain == null) return null;

        return SaleItemEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .quantityInBaseUnits(domain.getQuantityInBaseUnits())
                .displayQuantity(domain.getDisplayQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .unitMeasureId(domain.getUnitMeasureId())
                .sale(saleEntity)
                .build();
    }

    public SaleDomain toDomain(SaleEntity entity) {
        if (entity == null) return null;

        List<SaleItemDomain> items = entity.getItems() != null 
                ? entity.getItems().stream().map(this::toItemDomain).collect(Collectors.toList())
                : List.of();

        return SaleDomain.fromPersistence(
                entity.getId(),
                entity.getSaleCode(),
                entity.getTotalAmount(),
                entity.getAmountReceived(),
                entity.getChangeGiven(),
                entity.getPaymentMethodId(),
                entity.getStatusId(),
                entity.getClientId(),
                entity.getOutletId(),
                entity.getEmployeeId(),
                entity.getSaleDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }

    public SaleItemDomain toItemDomain(SaleItemEntity entity) {
        if (entity == null) return null;

        return new SaleItemDomain(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantityInBaseUnits(),
                entity.getDisplayQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal(),
                entity.getUnitMeasureId()
        );
    }
}
