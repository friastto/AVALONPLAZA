package org.frias.avalon.domain.cashregister.infrastructure.mapper;

import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.infrastructure.entity.CashExpenseEntity;
import org.springframework.stereotype.Component;

@Component
public class CashExpenseMapper {

    public CashExpenseEntity toEntity(CashExpenseDomain domain) {
        if (domain == null) return null;

        return CashExpenseEntity.builder()
                .id(domain.getId())
                .cashSessionId(domain.getCashSessionId())
                .amount(domain.getAmount())
                .reason(domain.getReason())
                .registeredBy(domain.getRegisteredBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public CashExpenseDomain toDomain(CashExpenseEntity entity) {
        if (entity == null) return null;

        return new CashExpenseDomain(
                entity.getId(),
                entity.getCashSessionId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getRegisteredBy(),
                entity.getCreatedAt()
        );
    }
}
