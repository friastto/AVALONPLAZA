package org.frias.avalon.domain.subscription.infrastructure.adapter;

import org.frias.avalon.domain.subscription.domain.model.SubscriptionPaymentDomain;
import org.frias.avalon.domain.subscription.domain.port.SubscriptionPaymentRepositoryPort;
import org.frias.avalon.domain.subscription.infrastructure.entity.SubscriptionPaymentEntity;
import org.frias.avalon.domain.subscription.infrastructure.repository.JpaSubscriptionPaymentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that connects SubscriptionPaymentRepositoryPort with JPA.
 */
@Component
public class SubscriptionPaymentRepositoryAdapter implements SubscriptionPaymentRepositoryPort {

    private final JpaSubscriptionPaymentRepository jpa;

    public SubscriptionPaymentRepositoryAdapter(JpaSubscriptionPaymentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SubscriptionPaymentDomain save(SubscriptionPaymentDomain domain) {
        SubscriptionPaymentEntity entity = toEntity(domain);
        SubscriptionPaymentEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SubscriptionPaymentDomain> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SubscriptionPaymentDomain> findByWompiReference(String wompiReference) {
        return jpa.findByWompiReference(wompiReference).map(this::toDomain);
    }

    @Override
    public Optional<SubscriptionPaymentDomain> findByWompiTransactionId(String wompiTransactionId) {
        return jpa.findByWompiTransactionId(wompiTransactionId).map(this::toDomain);
    }

    @Override
    public List<SubscriptionPaymentDomain> findByOutletSubscriptionId(Long outletSubscriptionId) {
        return jpa.findByOutletSubscriptionId(outletSubscriptionId).stream().map(this::toDomain).toList();
    }

    private SubscriptionPaymentEntity toEntity(SubscriptionPaymentDomain domain) {
        return SubscriptionPaymentEntity.builder()
                .id(domain.id())
                .outletSubscriptionId(domain.outletSubscriptionId())
                .outletId(domain.outletId())
                .companyId(domain.companyId())
                .wompiTransactionId(domain.wompiTransactionId())
                .wompiReference(domain.wompiReference())
                .paymentMethodType(domain.paymentMethodType())
                .amountInCents(domain.amountInCents())
                .currency(domain.currency())
                .status(domain.status())
                .checksumSent(domain.checksumSent())
                .webhookPayload(domain.webhookPayload())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }

    private SubscriptionPaymentDomain toDomain(SubscriptionPaymentEntity entity) {
        return new SubscriptionPaymentDomain(
                entity.getId(),
                entity.getOutletSubscriptionId(),
                entity.getOutletId(),
                entity.getCompanyId(),
                entity.getWompiTransactionId(),
                entity.getWompiReference(),
                entity.getPaymentMethodType(),
                entity.getAmountInCents(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getChecksumSent(),
                entity.getWebhookPayload(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
