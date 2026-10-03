package org.frias.avalon.domain.subscription.infrastructure.adapter;

import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.frias.avalon.domain.subscription.infrastructure.entity.OutletSubscriptionEntity;
import org.frias.avalon.domain.subscription.infrastructure.repository.JpaOutletSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that connects OutletSubscriptionRepositoryPort with JPA.
 */
@Component
public class OutletSubscriptionRepositoryAdapter implements OutletSubscriptionRepositoryPort {

    private final JpaOutletSubscriptionRepository jpa;

    public OutletSubscriptionRepositoryAdapter(JpaOutletSubscriptionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OutletSubscriptionDomain save(OutletSubscriptionDomain domain) {
        OutletSubscriptionEntity entity = toEntity(domain);
        OutletSubscriptionEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OutletSubscriptionDomain> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<OutletSubscriptionDomain> findByOutletId(Long outletId) {
        return jpa.findByOutletId(outletId).map(this::toDomain);
    }

    @Override
    public List<OutletSubscriptionDomain> findByCompanyId(Long companyId) {
        return jpa.findByCompanyId(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<OutletSubscriptionDomain> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private OutletSubscriptionEntity toEntity(OutletSubscriptionDomain domain) {
        return OutletSubscriptionEntity.builder()
                .id(domain.id())
                .outletId(domain.outletId())
                .companyId(domain.companyId())
                .statusId(domain.statusId())
                .billingDay(domain.billingDay())
                .amountCop(domain.amountCop())
                .trialEndsAt(domain.trialEndsAt())
                .currentPeriodStart(domain.currentPeriodStart())
                .currentPeriodEnd(domain.currentPeriodEnd())
                .gracePeriodEnd(domain.gracePeriodEnd())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }

    private OutletSubscriptionDomain toDomain(OutletSubscriptionEntity entity) {
        return new OutletSubscriptionDomain(
                entity.getId(),
                entity.getOutletId(),
                entity.getCompanyId(),
                entity.getStatusId(),
                entity.getBillingDay(),
                entity.getAmountCop(),
                entity.getTrialEndsAt(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getGracePeriodEnd(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
