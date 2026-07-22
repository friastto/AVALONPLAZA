package org.frias.avalon.domain.sale.application.usecase.sale.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchSalesUseCaseImpl implements SearchSalesUseCase {

    private final SaleRepositoryPort saleRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> search(Long outletId, String query) {
        Long finalOutletId = getEffectiveOutletId(outletId);
        return saleRepositoryPort.flexibleSearch(finalOutletId, query, PageRequest.of(0, 20)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getRecentSales(Long outletId) {
        Long finalOutletId = getEffectiveOutletId(outletId);
        return saleRepositoryPort.findRecentSales(finalOutletId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findByFlexibleCode(String codeOrSearch, Long outletId) {
        Long finalOutletId = getEffectiveOutletId(outletId);
        String cleanInput = codeOrSearch != null ? codeOrSearch.trim() : "";

        // 1. Intentar como UUID
        try {
            UUID uuid = UUID.fromString(cleanInput);
            SaleDomain sale = saleRepositoryPort.findByCode(uuid).orElse(null);
            if (sale != null) return mapToResponse(sale);
        } catch (IllegalArgumentException ignored) {}

        // 2. Intentar como ID numérico de venta
        try {
            Long saleId = Long.parseLong(cleanInput);
            SaleDomain sale = saleRepositoryPort.findById(saleId).orElse(null);
            if (sale != null) return mapToResponse(sale);
        } catch (NumberFormatException ignored) {}

        // 3. Buscar mediante búsqueda flexible (ShortCode YYYYMMDDHHMM, cédula, nombre)
        List<SaleDomain> results = saleRepositoryPort.flexibleSearch(finalOutletId, cleanInput, PageRequest.of(0, 1));
        if (!results.isEmpty()) {
            return mapToResponse(results.get(0));
        }

        throw new ResourceNotFoundException("No se encontró ninguna venta con el criterio o código: " + cleanInput);
    }

    private Long getEffectiveOutletId(Long outletId) {
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            return tenantOutletId != null ? tenantOutletId : outletId;
        }
        return outletId;
    }

    private SaleResponse mapToResponse(SaleDomain sale) {
        PersonDomain client = personRepositoryPort.findById(sale.getClientId())
                .orElse(null);

        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot payNode = masterTree.getById(sale.getPaymentMethodId());
        MasterRoot statusNode = masterTree.getById(sale.getStatusId());

        MasterDataResponseDto payDto = payNode != null
                ? new MasterDataResponseDto(payNode.getId(), payNode.getShortName(), payNode.getFullName())
                : null;

        MasterDataResponseDto statusDto = statusNode != null
                ? new MasterDataResponseDto(statusNode.getId(), statusNode.getShortName(), statusNode.getFullName())
                : null;

        List<SaleItemResponse> itemResponses = new ArrayList<>();
        if (sale.getItems() != null) {
            for (var item : sale.getItems()) {
                ProductDomain product = productOutletRepositoryPort.findById(item.getProductId()).orElse(null);
                String pName = product != null ? product.getName() : "Producto #" + item.getProductId();

                itemResponses.add(new SaleItemResponse(
                        item.getProductId(), pName, item.getDisplayQuantity(), item.getUnitPrice(), item.getSubtotal()
                ));
            }
        }

        return new SaleResponse(
                sale.getId(), sale.getSaleCode(), sale.getTotalAmount(),
                sale.getAmountReceived(), sale.getChangeGiven(), sale.getSaleDate(),
                payDto, statusDto,
                client != null ? client.getFullName() : "Cliente General",
                client != null ? client.getNumberid() : "",
                sale.getOutletId(), sale.getEmployeeId(), itemResponses
        );
    }
}
