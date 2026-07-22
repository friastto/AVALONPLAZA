package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnMapper {

    public ReturnEntity toEntity(ReturnDomain domain) {
        if (domain == null) return null;

        ReturnEntity entity = ReturnEntity.builder()
                .id(domain.getId())
                .returnCode(domain.getReturnCode())
                .originalSaleId(domain.getOriginalSaleId())
                .totalRefundAmount(domain.getTotalRefundAmount())
                .reason(domain.getReason())
                .resolutionType(domain.getResolutionType())
                .statusId(domain.getStatusId())
                .employeeId(domain.getEmployeeId())
                .outletId(domain.getOutletId())
                .clientId(domain.getClientId())
                .returnDate(domain.getReturnDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getItems() != null) {
            List<ReturnItemEntity> itemEntities = domain.getItems().stream()
                    .map(item -> toItemEntity(item, entity))
                    .collect(Collectors.toList());
            entity.setItems(itemEntities);
        }
        return entity;
    }

    public ReturnItemEntity toItemEntity(ReturnItemDomain domain, ReturnEntity returnEntity) {
        if (domain == null) return null;
        return ReturnItemEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .quantityInBaseUnits(domain.getQuantityInBaseUnits())
                .displayQuantity(domain.getDisplayQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .unitMeasureId(domain.getUnitMeasureId())
                .returnEntity(returnEntity)
                .build();
    }

    public ReturnDomain toDomain(ReturnEntity entity) {
        if (entity == null) return null;

        List<ReturnItemDomain> items = entity.getItems() != null
                ? entity.getItems().stream().map(this::toItemDomain).collect(Collectors.toList())
                : List.of();

        return ReturnDomain.fromPersistence(
                entity.getId(),
                entity.getReturnCode(),
                entity.getOriginalSaleId(),
                entity.getTotalRefundAmount(),
                entity.getReason(),
                entity.getResolutionType(),
                entity.getStatusId(),
                entity.getEmployeeId(),
                entity.getOutletId(),
                entity.getClientId(),
                entity.getReturnDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }

    public ReturnItemDomain toItemDomain(ReturnItemEntity entity) {
        if (entity == null) return null;
        return new ReturnItemDomain(
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
