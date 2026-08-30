package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyDashboardResponse;
import org.frias.avalon.domain.company.application.dto.response.OutletSalesPerformanceDto;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaSaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of GetCompanyDashboardUseCase.
 * Consolidates real sales, payment methods, transaction counts, and store performance
 * across all outlets belonging to a company with dynamic time filters.
 */
@Service
public class GetCompanyDashboardUseCaseImpl implements GetCompanyDashboardUseCase {

    private final JpaCompanyRepository companyRepository;
    private final JpaOutletRepository outletRepository;
    private final JpaSaleRepository saleRepository;

    public GetCompanyDashboardUseCaseImpl(
            JpaCompanyRepository companyRepository,
            JpaOutletRepository outletRepository,
            JpaSaleRepository saleRepository
    ) {
        this.companyRepository = companyRepository;
        this.outletRepository = outletRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDashboardResponse execute(Long companyId, String period, Long outletId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found"));

        String normalizedPeriod = (period != null && !period.isBlank()) ? period.toUpperCase().trim() : "MES";

        List<Outlet> companyOutlets = outletRepository.findByCompanyId(companyId);
        List<Outlet> targetOutlets;

        if (outletId != null) {
            targetOutlets = companyOutlets.stream()
                    .filter(o -> Objects.equals(o.getId(), outletId))
                    .toList();
        } else {
            targetOutlets = companyOutlets;
        }

        if (targetOutlets.isEmpty()) {
            return new CompanyDashboardResponse(
                    companyId,
                    company.getName(),
                    normalizedPeriod,
                    outletId,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0.0,
                    0L,
                    BigDecimal.ZERO,
                    Collections.emptyMap(),
                    Collections.emptyList()
            );
        }

        List<Long> targetOutletIds = targetOutlets.stream()
                .map(Outlet::getId)
                .filter(Objects::nonNull)
                .toList();

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        LocalDate today = LocalDate.now();

        switch (normalizedPeriod) {
            case "HOY" -> {
                startDate = today.atStartOfDay();
                endDate = today.atTime(LocalTime.MAX);
            }
            case "ANIO", "YEAR" -> {
                startDate = today.withDayOfYear(1).atStartOfDay();
                endDate = today.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
            }
            case "HISTORICO", "ALL" -> {
                startDate = null;
                endDate = null;
            }
            default -> { // "MES"
                normalizedPeriod = "MES";
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            }
        }

        List<SaleEntity> sales;
        if (startDate != null && endDate != null) {
            sales = saleRepository.findByOutletIdInAndSaleDateBetween(targetOutletIds, startDate, endDate);
        } else {
            sales = saleRepository.findByOutletIdIn(targetOutletIds);
        }

        BigDecimal totalSales = sales.stream()
                .map(SaleEntity::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long transactionCount = sales.size();

        BigDecimal averageTicket = transactionCount > 0
                ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal netProfit = totalSales.subtract(totalExpenses);

        double profitMarginPercentage = totalSales.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalSales, 4, RoundingMode.HALF_UP).doubleValue() * 100.0
                : 0.0;

        Map<String, BigDecimal> salesByPaymentMethod = new LinkedHashMap<>();
        for (SaleEntity s : sales) {
            String methodKey = resolvePaymentMethodName(s.getPaymentMethodId());
            BigDecimal current = salesByPaymentMethod.getOrDefault(methodKey, BigDecimal.ZERO);
            salesByPaymentMethod.put(methodKey, current.add(s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO));
        }

        Map<Long, List<SaleEntity>> salesByOutletId = sales.stream()
                .collect(Collectors.groupingBy(SaleEntity::getOutletId));

        List<OutletSalesPerformanceDto> outletSalesList = new ArrayList<>();
        for (Outlet o : targetOutlets) {
            List<SaleEntity> outletSales = salesByOutletId.getOrDefault(o.getId(), Collections.emptyList());
            BigDecimal outletTotal = outletSales.stream()
                    .map(SaleEntity::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long outletTxCount = outletSales.size();
            double pct = totalSales.compareTo(BigDecimal.ZERO) > 0
                    ? outletTotal.divide(totalSales, 4, RoundingMode.HALF_UP).doubleValue() * 100.0
                    : 0.0;

            outletSalesList.add(new OutletSalesPerformanceDto(
                    o.getId(),
                    o.getName() != null ? o.getName() : "Tienda #" + o.getId(),
                    outletTotal,
                    outletTxCount,
                    Math.round(pct * 100.0) / 100.0
            ));
        }

        outletSalesList.sort((a, b) -> b.totalSales().compareTo(a.totalSales()));

        return new CompanyDashboardResponse(
                companyId,
                company.getName(),
                normalizedPeriod,
                outletId,
                totalSales,
                totalExpenses,
                netProfit,
                Math.round(profitMarginPercentage * 100.0) / 100.0,
                transactionCount,
                averageTicket,
                salesByPaymentMethod,
                outletSalesList
        );
    }

    private String resolvePaymentMethodName(Long paymentMethodId) {
        if (paymentMethodId == null) return "OTRO";
        if (paymentMethodId == 139L) return "EFECTIVO";
        if (paymentMethodId == 151L) return "FIADO";
        return "METODO_" + paymentMethodId;
    }
}
