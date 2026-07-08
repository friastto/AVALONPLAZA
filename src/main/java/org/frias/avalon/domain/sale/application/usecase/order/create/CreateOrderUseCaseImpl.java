package org.frias.avalon.domain.sale.application.usecase.order.create;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateOrderRequest;
import org.frias.avalon.domain.sale.application.dto.request.OrderItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.OrderItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SaleWeightConversionService weightConversionService;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        // Tenant Isolation
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            if (!tenantOutletId.equals(request.outletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para registrar pedidos en otra tienda.");
            }
        }

        // Resolver Estado del Pedido: Pendiente ("PEN")
        Long pendingStatusId = masterDataRepositoryPort.getIdByCode("PEN");
        if (pendingStatusId == null) {
            throw new IllegalStateException("Estado Pendiente ('PEN') no encontrado en MasterData.");
        }

        MasterTree masterTree = masterTreeProvider.getTree();

        List<OrderItemDomain> itemDomains = new ArrayList<>();
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {
            ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + itemReq.productId() + " no existe"));

            if (!product.getOutletId().equals(request.outletId())) {
                throw new BusinessException("El producto '" + product.getName() + "' no pertenece a la tienda del pedido.");
            }

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            if (unitNode == null) {
                throw new DomainValidationException("La unidad de medida del producto " + product.getName() + " no es válida.");
            }
            String unitCode = unitNode.getShortName();

            // Parsea la cantidad
            Integer qtyInBaseUnits;
            BigDecimal displayQty;
            
            boolean isWeighable = weightConversionService.isWeighable(unitCode);
            if (isWeighable) {
                try {
                    String cleanQty = itemReq.quantity().replace(",", ".");
                    displayQty = new BigDecimal(cleanQty);
                } catch (NumberFormatException e) {
                    throw new BusinessException("La cantidad '" + itemReq.quantity() + "' no es un decimal válido para el producto: " + product.getName());
                }
                qtyInBaseUnits = weightConversionService.convertToBaseUnit(displayQty, unitCode);
            } else {
                try {
                    qtyInBaseUnits = Integer.parseInt(itemReq.quantity());
                    displayQty = new BigDecimal(qtyInBaseUnits);
                } catch (NumberFormatException e) {
                    throw new BusinessException("La cantidad '" + itemReq.quantity() + "' debe ser un entero para el producto: " + product.getName());
                }
            }

            if (qtyInBaseUnits <= 0) {
                throw new BusinessException("La cantidad para el producto " + product.getName() + " debe ser mayor a cero.");
            }

            // OJO: En la creación del pedido no descontamos stock, solo calculamos precios.
            BigDecimal subtotal;
            if (isWeighable) {
                BigDecimal factor;
                switch (unitCode.toUpperCase()) {
                    case "KG":
                    case "L":
                        factor = new BigDecimal("1000");
                        break;
                    case "LB":
                        factor = new BigDecimal("453.59237");
                        break;
                    default:
                        factor = BigDecimal.ONE;
                }
                BigDecimal pricePerBaseUnit = product.getPrice().divide(factor, 6, RoundingMode.HALF_UP);
                subtotal = pricePerBaseUnit.multiply(new BigDecimal(qtyInBaseUnits)).setScale(2, RoundingMode.HALF_UP);
            } else {
                subtotal = product.getPrice().multiply(displayQty).setScale(2, RoundingMode.HALF_UP);
            }

            String displayQtyStr = weightConversionService.formatFromBaseUnit(qtyInBaseUnits, unitCode);

            OrderItemDomain itemDomain = new OrderItemDomain(
                    null,
                    product.getId(),
                    qtyInBaseUnits,
                    displayQtyStr,
                    product.getPrice(),
                    subtotal,
                    product.getUnitMeasureId()
            );
            itemDomains.add(itemDomain);

            itemResponses.add(new OrderItemResponse(
                    product.getId(),
                    product.getName(),
                    displayQtyStr,
                    product.getPrice(),
                    subtotal
            ));
        }

        // Crear y guardar el Pedido
        OrderDomain orderDomain = OrderDomain.create(
                request.paymentMethodId(),
                pendingStatusId,
                request.outletId(),
                itemDomains
        );

        OrderDomain savedOrder = orderRepositoryPort.save(orderDomain);

        // Mapear respuesta
        MasterRoot payMethodNode = masterTree.getById(savedOrder.getPaymentMethodId());
        MasterRoot statusNode = masterTree.getById(savedOrder.getStatusId());

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

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getTotalAmount(),
                savedOrder.getOrderDate(),
                payDto,
                statusDto,
                savedOrder.getOutletId(),
                itemResponses
        );
    }
}
