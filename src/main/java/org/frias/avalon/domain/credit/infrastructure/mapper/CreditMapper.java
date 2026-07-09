package org.frias.avalon.domain.credit.infrastructure.mapper;

import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.credit.infrastructure.entity.CreditAccountEntity;
import org.frias.avalon.domain.credit.infrastructure.entity.CreditTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditAccountDomain toDomain(CreditAccountEntity entity) {
        if (entity == null) return null;
        return CreditAccountDomain.reconstruct(
                entity.getId(),
                entity.getClientId(),
                entity.getOutletId(),
                entity.getCreditLimit(),
                entity.getCurrentDebt(),
                entity.getStatusId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CreditAccountEntity toEntity(CreditAccountDomain domain) {
        if (domain == null) return null;
        return CreditAccountEntity.builder()
                .id(domain.getId())
                .clientId(domain.getClientId())
                .outletId(domain.getOutletId())
                .creditLimit(domain.getCreditLimit())
                .currentDebt(domain.getCurrentDebt())
                .statusId(domain.getStatusId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public CreditTransactionDomain toDomain(CreditTransactionEntity entity) {
        if (entity == null) return null;
        return CreditTransactionDomain.reconstruct(
                entity.getId(),
                entity.getCreditAccountId(),
                entity.getSaleId(),
                entity.getType(),
                entity.getAmount(),
                entity.getPreviousDebt(),
                entity.getNewDebt(),
                entity.getNotes(),
                entity.getRegisteredBy(),
                entity.getCreatedAt()
        );
    }

    public CreditTransactionEntity toEntity(CreditTransactionDomain domain) {
        if (domain == null) return null;
        return CreditTransactionEntity.builder()
                .id(domain.getId())
                .creditAccountId(domain.getCreditAccountId())
                .saleId(domain.getSaleId())
                .type(domain.getType())
                .amount(domain.getAmount())
                .previousDebt(domain.getPreviousDebt())
                .newDebt(domain.getNewDebt())
                .notes(domain.getNotes())
                .registeredBy(domain.getRegisteredBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
