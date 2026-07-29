package org.frias.avalon.domain.sale.application.port;

import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepositoryPort {

    SaleDomain save(SaleDomain sale);

    Optional<SaleDomain> findByCode(UUID code);

    Optional<SaleDomain> findById(Long id);

    Page<SaleDomain> findByOutletId(Long outletId, Pageable pageable);

    java.util.List<SaleDomain> flexibleSearch(Long outletId, String query, Pageable pageable);

    java.util.List<SaleDomain> findRecentSales(Long outletId);

    java.util.List<SaleDomain> findByOutletAndEmployeeAndDateBetween(Long outletId, Long employeeId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    java.util.List<SaleDomain> findByOutletAndDateBetween(Long outletId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
