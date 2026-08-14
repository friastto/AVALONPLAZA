package org.frias.avalon.domain.company.domain.port;

import org.frias.avalon.domain.company.domain.model.CompanyDomain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port (interface) for Company domain.
 * Follows Clean Architecture: domain knows only ports, never JPA directly.
 */
public interface CompanyRepositoryPort {

    /** Persists a new company and returns the saved domain. */
    CompanyDomain save(CompanyDomain company);

    /** Finds a company by its NIT identifier. */
    Optional<CompanyDomain> findByNit(String nit);

    /** Finds a company by its internal ID. */
    Optional<CompanyDomain> findById(Long id);

    /** Returns all registered companies. */
    List<CompanyDomain> findAll();

    /** Returns companies filtered by statusId. */
    List<CompanyDomain> findByStatusId(Long statusId);

    /** Updates default cash threshold amount for a company. */
    void updateDefaultThreshold(Long companyId, java.math.BigDecimal thresholdAmount);
}
