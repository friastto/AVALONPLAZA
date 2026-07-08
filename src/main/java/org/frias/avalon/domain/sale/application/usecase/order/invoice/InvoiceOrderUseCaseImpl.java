package org.frias.avalon.domain.sale.application.usecase.order.invoice;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceOrderUseCaseImpl implements InvoiceOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional
    public SaleResponse execute(UUID orderCode, String clientNumberid, BigDecimal amountReceived) {
        // --- 1. Buscar Pedido ---
        OrderDomain order = orderRepositoryPort.findByCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con código '" + orderCode + "' no encontrado."));

        // --- 2. Validar Tenant Isolation ---
        UserContext userContext = currentUserProvider.getCurrentUserContext();
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            if (!tenantOutletId.equals(order.getOutletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para facturar un pedido de otra tienda.");
            }
        }

        // --- 3. Validar Estado del Pedido (Debe ser "PEN" - Pendiente) ---
        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot currentStatus = masterTree.getById(order.getStatusId());
        if (currentStatus == null || !currentStatus.getShortName().equals("PEN")) {
            throw new BusinessException("El pedido no está en estado PENDIENTE y no puede ser facturado.");
        }

        // --- 4. Resolver Empleado ---
        UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe en el sistema"));
        Long employeeId = userDomain.getPersonId();
        if (employeeId == null) {
            throw new BusinessException("El usuario actual no tiene un registro de persona (empleado) asociado");
        }

        // --- 5. Resolver Cliente ---
        PersonDomain clientDomain = personRepositoryPort.findByNumberid(clientNumberid)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con identificación '" + clientNumberid + "' no encontrado."));

        // --- 6. Resolver Estados Nuevos ---
        Long activeSaleStatusId = masterDataRepositoryPort.getIdByCode("ACT"); // Venta activa
        Long completedOrderStatusId = masterDataRepositoryPort.getIdByCode("COM"); // Pedido completado

        if (activeSaleStatusId == null || completedOrderStatusId == null) {
            throw new IllegalStateException("Estados ('ACT' o 'COM') no encontrados en MasterData.");
        }

        // --- 7. Procesar Ítems y Descontar Inventario ---
        List<SaleItemDomain> saleItems = new ArrayList<>();
        List<SaleItemResponse> saleItemResponses = new ArrayList<>();

        for (OrderItemDomain orderItem : order.getItems()) {
            ProductDomain product = productOutletRepositoryPort.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + orderItem.getProductId() + " no encontrado"));

            // Descontar Stock
            product.removeStock(orderItem.getQuantityInBaseUnits());
            productOutletRepositoryPort.save(product);

            // Crear ítem de venta equivalente
            SaleItemDomain saleItem = new SaleItemDomain(
                    null,
                    product.getId(),
                    orderItem.getQuantityInBaseUnits(),
                    orderItem.getDisplayQuantity(),
                    orderItem.getUnitPrice(),
                    orderItem.getSubtotal(),
                    orderItem.getUnitMeasureId()
            );
            saleItems.add(saleItem);

            saleItemResponses.add(new SaleItemResponse(
                    product.getId(),
                    product.getName(),
                    orderItem.getDisplayQuantity(),
                    orderItem.getUnitPrice(),
                    orderItem.getSubtotal()
            ));
        }

        // --- 8. Crear y pagar Venta ---
        SaleDomain saleDomain = SaleDomain.create(
                order.getPaymentMethodId(),
                activeSaleStatusId,
                clientDomain.getId(),
                order.getOutletId(),
                employeeId,
                saleItems
        );

        if (amountReceived != null) {
            saleDomain.applyPayment(amountReceived);
        } else {
            // Si es por transferencia o tarjeta y no se ingresa cantidad, se asume pago exacto
            saleDomain.applyPayment(saleDomain.getTotalAmount());
        }

        // Guardar Venta
        SaleDomain savedSale = saleRepositoryPort.save(saleDomain);

        // --- 9. Actualizar Pedido a COMPLETADO ---
        order.markAsInvoiced(completedOrderStatusId);
        orderRepositoryPort.save(order);

        // --- 10. Mapear Respuesta ---
        MasterRoot payMethodNode = masterTree.getById(savedSale.getPaymentMethodId());
        MasterRoot statusNode = masterTree.getById(savedSale.getStatusId());

        MasterDataResponseDto payDto = new MasterDataResponseDto(
                payMethodNode.getId(),
                payMethodNode.getShortName(),
                payMethodNode.getFullName()
        );

        MasterDataResponseDto statusDto = new MasterDataResponseDto(
                statusNode.getId(),
                statusNode.getShortName(),
                statusNode.getFullName()
        );

        return new SaleResponse(
                savedSale.getId(),
                savedSale.getSaleCode(),
                savedSale.getTotalAmount(),
                savedSale.getAmountReceived(),
                savedSale.getChangeGiven(),
                savedSale.getSaleDate(),
                payDto,
                statusDto,
                clientDomain.getFullName(),
                clientDomain.getNumberid(),
                savedSale.getOutletId(),
                savedSale.getEmployeeId(),
                saleItemResponses
        );
    }
}
