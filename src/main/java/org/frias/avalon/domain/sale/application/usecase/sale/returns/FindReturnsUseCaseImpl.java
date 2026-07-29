package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.sale.application.dto.response.ReturnItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindReturnsUseCaseImpl implements FindReturnsUseCase {

    private final ReturnRepositoryPort returnRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;

    @Override
    public Optional<ReturnResponse> findByCode(UUID returnCode) {
        return returnRepositoryPort.findByCode(returnCode)
                .map(this::toResponse);
    }

    @Override
    public Page<ReturnResponse> findByOutlet(Long outletId, Pageable pageable) {
        return returnRepositoryPort.findByOutletId(outletId, pageable)
                .map(this::toResponse);
    }

    private ReturnResponse toResponse(ReturnDomain domain) {
        SaleDomain originalSale = saleRepositoryPort.findById(domain.getOriginalSaleId())
                .orElse(null);

        PersonDomain client = personRepositoryPort.findById(domain.getClientId())
                .orElse(null);

        List<ReturnItemResponse> items = domain.getItems().stream()
                .map(item -> new ReturnItemResponse(
                        item.getProductId(),
                        "Producto #" + item.getProductId(),
                        item.getDisplayQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new ReturnResponse(
                domain.getId(),
                domain.getReturnCode(),
                originalSale != null ? originalSale.getSaleCode() : null,
                domain.getOriginalSaleId(),
                domain.getTotalRefundAmount(),
                domain.getReason(),
                domain.getNotes(),
                domain.getResolutionType(),
                "DEV",
                client != null ? client.getFullName() : "Desconocido",
                client != null ? client.getNumberid() : "",
                domain.getOutletId(),
                domain.getEmployeeId(),
                domain.getReturnDate(),
                items
        );
    }
}
