package org.frias.avalon.domain.cashregister.infrastructure.mapper;

import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.infrastructure.entity.CashSessionEntity;
import org.springframework.stereotype.Component;

@Component
public class CashSessionMapper {

    public CashSessionEntity toEntity(CashSessionDomain domain) {
        if (domain == null) return null;

        return CashSessionEntity.builder()
                .id(domain.getId())
                .outletId(domain.getOutletId())
                .employeeId(domain.getEmployeeId())
                .openedAt(domain.getOpenedAt())
                .closedAt(domain.getClosedAt())
                .initialBase(domain.getInitialBase())
                .expectedCash(domain.getExpectedCash())
                .actualCash(domain.getActualCash())
                .difference(domain.getDifference())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public CashSessionDomain toDomain(CashSessionEntity entity) {
        if (entity == null) return null;

        return CashSessionDomain.fromPersistence(
                entity.getId(),
                entity.getOutletId(),
                entity.getEmployeeId(),
                entity.getOpenedAt(),
                entity.getClosedAt(),
                entity.getInitialBase(),
                entity.getExpectedCash(),
                entity.getActualCash(),
                entity.getDifference(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity toPickupEntity(org.frias.avalon.domain.cashregister.domain.CashPickupDomain domain) {
        if (domain == null) return null;
        return org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .employeeId(domain.getEmployeeId())
                .amount(domain.getAmount())
                .reason(domain.getReason())
                .pickupTime(domain.getPickupTime())
                .build();
    }

    public org.frias.avalon.domain.cashregister.domain.CashPickupDomain toPickupDomain(org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity entity) {
        if (entity == null) return null;
        return org.frias.avalon.domain.cashregister.domain.CashPickupDomain.fromPersistence(
                entity.getId(),
                entity.getSessionId(),
                entity.getEmployeeId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getPickupTime()
        );
    }
}
