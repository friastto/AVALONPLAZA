package org.frias.avalon.domain.company.infrastructure.adapter;

import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that bridges the CompanyRepositoryPort (domain) to JPA (infrastructure).
 * Handles mapping between CompanyEntity and CompanyDomain.
 */
@Component
public class CompanyRepositoryAdapter implements CompanyRepositoryPort {

    private final JpaCompanyRepository jpa;

    public CompanyRepositoryAdapter(JpaCompanyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CompanyDomain save(CompanyDomain domain) {
        CompanyEntity entity = toEntity(domain);
        CompanyEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CompanyDomain> findByNit(String nit) {
        return jpa.findByNit(nit).map(this::toDomain);
    }

    @Override
    public Optional<CompanyDomain> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<CompanyDomain> findAll() {
        return jpa.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CompanyDomain> findByStatusId(Long statusId) {
        return jpa.findByStatusId(statusId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateDefaultThreshold(Long companyId, java.math.BigDecimal thresholdAmount) {
        jpa.findById(companyId).ifPresent(entity -> {
            entity.setDefaultCashThresholdAmount(thresholdAmount);
            jpa.save(entity);
        });
    }

    // --- Internal mappers (no separate MapStruct needed for simple flat entity) ---

    private CompanyEntity toEntity(CompanyDomain domain) {
        return CompanyEntity.builder()
                .id(domain.id())
                .nit(domain.nit())
                .name(domain.name())
                .email(domain.email())
                .statusId(domain.statusId())
                .defaultCashThresholdAmount(domain.defaultCashThresholdAmount())
                .build();
    }

    private CompanyDomain toDomain(CompanyEntity entity) {
        return new CompanyDomain(
                entity.getId(),
                entity.getNit(),
                entity.getName(),
                entity.getEmail(),
                entity.getStatusId(),
                entity.getDefaultCashThresholdAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
