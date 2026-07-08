package org.frias.avalon.domain.sale.application.usecase.sale.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
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
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllSalesUseCaseImpl implements FindAllSalesUseCase {

    private final SaleRepositoryPort saleRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponse> execute(Long outletId, Pageable pageable) {
        // Tenant Isolation
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        Long finalOutletId = outletId;

        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            if (outletId != null && !tenantOutletId.equals(outletId)) {
                throw new BusinessException("Acceso denegado: No tienes permisos para listar ventas de otra tienda.");
            }
            finalOutletId = tenantOutletId;
        }

        if (finalOutletId == null) {
            throw new BusinessException("Se requiere especificar el ID de la tienda para listar las ventas.");
        }

        MasterTree masterTree = masterTreeProvider.getTree();

        return saleRepositoryPort.findByOutletId(finalOutletId, pageable)
                .map(sale -> {
                    PersonDomain client = personRepositoryPort.findById(sale.getClientId())
                            .orElseThrow(() -> new ResourceNotFoundException("Cliente asociado a la venta no encontrado"));

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
                });
    }
}
