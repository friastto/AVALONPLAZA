package org.frias.avalon.domain.inventory.infrastructure.mapper;

import org.frias.avalon.domain.inventory.application.dto.AuditItemDto;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.AuditSignatureDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSignatureEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class AuditMapper {

    public AuditItemDto toItemDto(InventoryAuditItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditItemDto(
                entity.getId(),
                entity.getAuditSession() != null ? entity.getAuditSession().getId() : null,
                entity.getProductOutletId(),
                entity.getProductName(),
                entity.getBarcode(),
                entity.getUnitMeasure(),
                entity.getUnitPrice(),
                entity.getSoftwareStock(),
                entity.getSoftwareValue(),
                entity.getPhysicalStock(),
                entity.getPhysicalValue(),
                entity.getDifferenceStock(),
                entity.getDifferenceValue(),
                entity.getDiscrepancyReason(),
                entity.getCountedByUserId(),
                entity.getCountedByName(),
                entity.getStatus(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : ""
        );
    }

    public AuditSignatureDto toSignatureDto(InventoryAuditSignatureEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditSignatureDto(
                entity.getId(),
                entity.getSignerType(),
                entity.getSignerUserId(),
                entity.getSignerName(),
                entity.getSignatureBase64(),
                entity.getSignedAt() != null ? entity.getSignedAt().toString() : ""
        );
    }

    public AuditSessionDetailResponse toDetailResponse(
            InventoryAuditSessionEntity session,
            List<InventoryAuditItemEntity> items,
            List<InventoryAuditSignatureEntity> signatures
    ) {
        if (session == null) {
            return null;
        }

        List<AuditItemDto> itemDtos = (items != null)
                ? items.stream().map(this::toItemDto).toList()
                : Collections.emptyList();

        List<AuditSignatureDto> signatureDtos = (signatures != null)
                ? signatures.stream().map(this::toSignatureDto).toList()
                : Collections.emptyList();

        int totalItems = itemDtos.size();
        int countedItems = (int) itemDtos.stream()
                .filter(i -> "COUNTED".equalsIgnoreCase(i.status()) || "APPROVED".equalsIgnoreCase(i.status()))
                .count();
        int discrepancyItems = (int) itemDtos.stream()
                .filter(i -> i.differenceStock() != null && i.differenceStock().compareTo(BigDecimal.ZERO) != 0)
                .count();

        return new AuditSessionDetailResponse(
                session.getId(),
                session.getOutletId(),
                session.getCompanyId(),
                session.getStatus(),
                session.getOpenedByUserId(),
                session.getOpenedByName(),
                session.getClosedByUserId(),
                session.getClosedByName(),
                session.getNotes(),
                session.getTotalSoftwareValue() != null ? session.getTotalSoftwareValue() : BigDecimal.ZERO,
                session.getTotalPhysicalValue() != null ? session.getTotalPhysicalValue() : BigDecimal.ZERO,
                session.getTotalDifferenceValue() != null ? session.getTotalDifferenceValue() : BigDecimal.ZERO,
                session.getOpenedAt() != null ? session.getOpenedAt().toString() : "",
                session.getClosedAt() != null ? session.getClosedAt().toString() : null,
                totalItems,
                countedItems,
                discrepancyItems,
                itemDtos,
                signatureDtos
        );
    }
}
