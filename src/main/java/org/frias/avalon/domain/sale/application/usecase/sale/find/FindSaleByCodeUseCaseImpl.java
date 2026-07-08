package org.frias.avalon.domain.sale.application.usecase.sale.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
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
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindSaleByCodeUseCaseImpl implements FindSaleByCodeUseCase {

    private final SaleRepositoryPort saleRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    @Override
    @Transactional(readOnly = true)
    public SaleResponse execute(UUID code) {
        SaleDomain sale = saleRepositoryPort.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con código '" + code + "' no encontrada."));

        PersonDomain client = personRepositoryPort.findById(sale.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente asociado a la venta no encontrado"));

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
        for (SaleItemDomain item : sale.getItems()) {
            ProductDomain product = productOutletRepositoryPort.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + item.getProductId() + " no encontrado"));

            itemResponses.add(new SaleItemResponse(
                    product.getId(),
                    product.getName(),
                    item.getDisplayQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            ));
        }

        return new SaleResponse(
                sale.getId(),
                sale.getSaleCode(),
                sale.getTotalAmount(),
                sale.getAmountReceived(),
                sale.getChangeGiven(),
                sale.getSaleDate(),
                payDto,
                statusDto,
                client.getFullName(),
                client.getNumberid(),
                sale.getOutletId(),
                sale.getEmployeeId(),
                itemResponses
        );
    }
}
