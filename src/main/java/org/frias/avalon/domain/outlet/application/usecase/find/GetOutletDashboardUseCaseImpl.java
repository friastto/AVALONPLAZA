package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.CashRegisterMonitoringDto;
import org.frias.avalon.domain.outlet.application.dto.response.HourlySalesDto;
import org.frias.avalon.domain.outlet.application.dto.response.KpiMetricsDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDashboardResponse;
import org.frias.avalon.domain.outlet.application.dto.response.StockAlertDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Implementacion del Caso de Uso para la visualizacion de metricas y alertas reales
 * del Tablero de Control de la tienda en su esquema multi-tenant sin mocks ni datos demo.
 */
@Service
public class GetOutletDashboardUseCaseImpl implements GetOutletDashboardUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final CashSessionRepositoryPort cashSessionRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final TransactionTemplate transactionTemplate;

    public GetOutletDashboardUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            SaleRepositoryPort saleRepositoryPort,
            OutletRepositoryPort outletRepositoryPort,
            CashSessionRepositoryPort cashSessionRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            TransactionTemplate transactionTemplate
    ) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.saleRepositoryPort = saleRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.cashSessionRepositoryPort = cashSessionRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public OutletDashboardResponse execute(Long outletId, String filter) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;
        String normalizedFilter = (filter != null && !filter.isBlank()) ? filter.toUpperCase().trim() : "HOY";

        switch (normalizedFilter) {
            case "AYER" -> {
                LocalDate yesterday = today.minusDays(1);
                start = yesterday.atStartOfDay();
                end = yesterday.atTime(LocalTime.MAX);
            }
            case "SEMANA" -> {
                LocalDate startOfWeek = today.minusDays(6);
                start = startOfWeek.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            default -> { // "HOY"
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
        }

        OutletDomain outlet = outletRepositoryPort.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Outlet with ID " + outletId + " not found"));
        Long companyId = outlet.getCompanyId();

        Long prevCompanyId = TenantContext.getTenantId();
        Long prevOutletId = TenantContext.getTenantOutletId();

        try {
            if (companyId != null) {
                TenantContext.setTenantId(companyId);
            }
            TenantContext.setTenantOutletId(outletId);

            return transactionTemplate.execute(status -> {
                // 1. Consultar ventas reales de la tienda en el rango de fechas
                List<SaleDomain> sales = saleRepositoryPort.findByOutletAndDateBetween(outletId, start, end);
                if (sales == null) {
                    sales = Collections.emptyList();
                }

                // 2. Calcular KPIs reales O(1) con MasterTree
                MasterTree tree = masterTreeProvider.getTree();
                BigDecimal totalCash = BigDecimal.ZERO;
                BigDecimal totalTransfer = BigDecimal.ZERO;
                BigDecimal totalSalesAmount = BigDecimal.ZERO;

                for (SaleDomain sale : sales) {
                    BigDecimal amount = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;
                    totalSalesAmount = totalSalesAmount.add(amount);

                    Long methodId = sale.getPaymentMethodId();
                    if (methodId != null) {
                        MasterRoot node = tree.getById(methodId);
                        if (node != null && tree.is(node, "EFE")) {
                            totalCash = totalCash.add(amount);
                        } else {
                            totalTransfer = totalTransfer.add(amount);
                        }
                    } else {
                        totalCash = totalCash.add(amount);
                    }
                }

                int totalCustomers = sales.size();
                BigDecimal averageTicket = totalCustomers > 0
                        ? totalSalesAmount.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                KpiMetricsDto kpis = new KpiMetricsDto(
                        totalCash,
                        totalTransfer,
                        totalCustomers,
                        averageTicket
                );

                // 3. Ventas por horas reales agrupadas en franjas estandar
                List<HourlySalesDto> hourlySales = calculateHourlySales(sales);

                // 4. Alertas de inventario critico reales (sin mocks ni fallbacks demo)
                Page<ProductDomain> productsPage = productOutletRepositoryPort.findAll(null, outletId, PageRequest.of(0, 100));
                List<ProductDomain> products = productsPage != null ? productsPage.getContent() : Collections.emptyList();

                List<StockAlertDto> alerts = new ArrayList<>();
                int minimumStockLimit = 5;
                for (ProductDomain p : products) {
                    if (p.getStock() <= minimumStockLimit) {
                        String alertType = p.getStock() == 0 ? "EXPIRED" : (p.getStock() <= 2 ? "EXPIRY_WARNING" : "MINIMUM");
                        alerts.add(new StockAlertDto(p.getName(), p.getStock(), minimumStockLimit, alertType));
                    }
                }

                // 5. Cajas y turnos reales de la tienda
                List<CashRegisterMonitoringDto> activeRegisters = resolveCashRegisters(outletId);

                return new OutletDashboardResponse(kpis, hourlySales, alerts, activeRegisters);
            });
        } finally {
            TenantContext.setTenantId(prevCompanyId);
            TenantContext.setTenantOutletId(prevOutletId);
        }
    }

    private List<HourlySalesDto> calculateHourlySales(List<SaleDomain> sales) {
        String[] slots = {"08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00"};
        Map<String, BigDecimal> hourlyMap = new LinkedHashMap<>();
        for (String slot : slots) {
            hourlyMap.put(slot, BigDecimal.ZERO);
        }

        for (SaleDomain sale : sales) {
            if (sale.getSaleDate() == null) continue;
            int hour = sale.getSaleDate().getHour();
            String slot;
            if (hour < 9) {
                slot = "08:00";
            } else if (hour < 11) {
                slot = "10:00";
            } else if (hour < 13) {
                slot = "12:00";
            } else if (hour < 15) {
                slot = "14:00";
            } else if (hour < 17) {
                slot = "16:00";
            } else if (hour < 19) {
                slot = "18:00";
            } else {
                slot = "20:00";
            }

            BigDecimal current = hourlyMap.get(slot);
            BigDecimal amount = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;
            hourlyMap.put(slot, current.add(amount));
        }

        List<HourlySalesDto> list = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : hourlyMap.entrySet()) {
            list.add(new HourlySalesDto(entry.getKey(), entry.getValue().setScale(0, RoundingMode.HALF_UP)));
        }
        return list;
    }

    private List<CashRegisterMonitoringDto> resolveCashRegisters(Long outletId) {
        List<CashSessionDomain> sessions = cashSessionRepositoryPort.findAllSessionsByOutlet(outletId);
        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        List<CashRegisterMonitoringDto> list = new ArrayList<>();

        for (CashSessionDomain session : sessions) {
            boolean isOpen = "OPEN".equalsIgnoreCase(session.getStatus());
            boolean isToday = session.getOpenedAt() != null && session.getOpenedAt().toLocalDate().isEqual(today);

            if (isOpen || isToday) {
                String employeeName = resolveEmployeeName(session.getEmployeeId());
                String role = isOpen ? "Cajero en Turno" : "Cajero (Turno Cerrado)";
                BigDecimal currentCash = isOpen
                        ? (session.getExpectedCash() != null ? session.getExpectedCash() : session.getInitialBase())
                        : (session.getActualCash() != null ? session.getActualCash() : (session.getExpectedCash() != null ? session.getExpectedCash() : BigDecimal.ZERO));

                list.add(new CashRegisterMonitoringDto(
                        employeeName,
                        role,
                        isOpen,
                        currentCash
                ));
            }
        }
        return list;
    }

    private String resolveEmployeeName(Long employeeId) {
        if (employeeId == null) return "Cajero General";
        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(employeeId);
        if (userOpt.isPresent()) {
            UserAvalonDomain user = userOpt.get();
            if (user.getPersonId() != null) {
                Optional<PersonDomain> personOpt = personRepositoryPort.findById(user.getPersonId());
                if (personOpt.isPresent()) {
                    PersonDomain p = personOpt.get();
                    String full = (p.getName() + " " + (p.getLastName() != null ? p.getLastName() : "")).trim();
                    if (!full.isBlank()) return full;
                }
            }
            if (user.getUserName() != null && !user.getUserName().isBlank()) {
                return user.getUserName();
            }
        }
        Optional<PersonDomain> personOpt = personRepositoryPort.findById(employeeId);
        if (personOpt.isPresent()) {
            PersonDomain p = personOpt.get();
            String full = (p.getName() + " " + (p.getLastName() != null ? p.getLastName() : "")).trim();
            if (!full.isBlank()) return full;
        }
        return "Empleado #" + employeeId;
    }
}
