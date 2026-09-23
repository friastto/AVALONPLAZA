package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateReturnRequest;
import org.frias.avalon.domain.sale.application.dto.request.ReturnItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.ReturnItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateReturnUseCaseImpl implements CreateReturnUseCase {

    private final ReturnRepositoryPort returnRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SaleWeightConversionService weightConversionService;
    private final CurrentUserProviderPort currentUserProvider;
    private final CreditRepositoryPort creditRepositoryPort;

    @Override
    @Transactional
    public ReturnResponse execute(CreateReturnRequest request) {

        // --- 1. Validate Tenant ---
        UserContext userContext = currentUserProvider.getCurrentUserContext();
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");

        // --- 2. Search Original Sale by saleCode or Omnichannel Order (orderCode) ---
        SaleDomain originalSale = saleRepositoryPort.findByCode(request.originalSaleCode()).orElse(null);

        if (originalSale == null) {
            // Try as omnichannel order by orderCode string or synthetic UUID
            String codeStr = request.originalSaleCode().toString();
            OrderDomain order = orderRepositoryPort.findByOrderCode(codeStr).orElse(null);
            if (order == null) {
                Long tenantOutletId = currentUserProvider.getCurrentOutletId();
                if (tenantOutletId != null) {
                    List<OrderDomain> orders = orderRepositoryPort.findAllByOutletId(tenantOutletId);
                    for (OrderDomain o : orders) {
                        if (o.getOrderCode() != null) {
                            try {
                                UUID synth = UUID.nameUUIDFromBytes(o.getOrderCode().getBytes(StandardCharsets.UTF_8));
                                if (synth.equals(request.originalSaleCode())) {
                                    order = o;
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
            if (order != null) {
                originalSale = convertOrderToSaleDomain(order);
            }
        }

        if (originalSale == null) {
            throw new ResourceNotFoundException(
                    "No se encontró ninguna venta con el código: " + request.originalSaleCode() +
                    ". Verifique que el ticket o código presentado sea de esta tienda.");
        }

        // Validate outlet access
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId != null && !tenantOutletId.equals(originalSale.getOutletId())) {
                throw new BusinessException("Acceso denegado: Esta venta pertenece a otra tienda.");
            }
        }

        // --- 3. Resolve employee ---
        UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        Long employeeId = userDomain.getPersonId();
        if (employeeId == null) {
            throw new BusinessException("El usuario actual no tiene un registro de persona (empleado) asociado");
        }

        // --- 4. Resolve client of original sale ---
        PersonDomain clientDomain;
        if (originalSale.getClientId() != null) {
            clientDomain = personRepositoryPort.findById(originalSale.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente de la venta original no encontrado"));
        } else {
            clientDomain = personRepositoryPort.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente general no encontrado"));
        }

        // --- 5. Resolve DEV master data status ---
        Long devStatusId = masterDataRepositoryPort.getIdByCode("DEV");
        if (devStatusId == null) {
            throw new IllegalStateException("Estado 'DEV' (Devuelto) no encontrado en MasterData.");
        }

        // --- 6. Validate and process returned items ---
        MasterTree masterTree = masterTreeProvider.getTree();
        List<ReturnItemDomain> returnItems = new ArrayList<>();
        List<ReturnItemResponse> itemResponses = new ArrayList<>();

        for (ReturnItemRequest itemReq : request.items()) {

            // Check that product exists in original sale
            SaleItemDomain originalItem = originalSale.getItems().stream()
                    .filter(si -> si.getProductId().equals(itemReq.productId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            "El producto con ID " + itemReq.productId() +
                            " no está en la venta original. No se puede devolver."));

            // Get product from catalog
            ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto " + itemReq.productId() + " no encontrado"));

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            String unitCode = unitNode != null ? unitNode.getShortName() : "UND";
            boolean isWeighable = weightConversionService.isWeighable(unitCode);

            // Parse quantity
            Integer qtyInBaseUnits;
            BigDecimal displayQty;

            if (isWeighable) {
                try {
                    displayQty = new BigDecimal(itemReq.quantity().replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new BusinessException("Cantidad inválida para producto pesable: " + product.getName());
                }
                qtyInBaseUnits = weightConversionService.convertToBaseUnit(displayQty, unitCode);
            } else {
                try {
                    qtyInBaseUnits = Integer.parseInt(itemReq.quantity());
                    displayQty = new BigDecimal(qtyInBaseUnits);
                } catch (NumberFormatException e) {
                    throw new BusinessException("La cantidad debe ser un entero para: " + product.getName());
                }
            }

            if (qtyInBaseUnits <= 0) {
                throw new BusinessException("La cantidad a devolver debe ser mayor a cero: " + product.getName());
            }

            // Validate not exceeding original sale
            if (qtyInBaseUnits > originalItem.getQuantityInBaseUnits()) {
                throw new BusinessException(
                        "La cantidad a devolver (" + qtyInBaseUnits + ") supera lo vendido (" +
                        originalItem.getQuantityInBaseUnits() + ") para: " + product.getName());
            }

            // Calculate return subtotal
            BigDecimal subtotal;
            if (isWeighable) {
                BigDecimal factor;
                switch (unitCode.toUpperCase()) {
                    case "KG": case "L": factor = new BigDecimal("1000"); break;
                    case "LB": factor = new BigDecimal("453.59237"); break;
                    default: factor = BigDecimal.ONE;
                }
                BigDecimal pricePerBaseUnit = product.getPrice().divide(factor, 6, RoundingMode.HALF_UP);
                subtotal = pricePerBaseUnit.multiply(new BigDecimal(qtyInBaseUnits)).setScale(2, RoundingMode.HALF_UP);
            } else {
                subtotal = product.getPrice().multiply(displayQty).setScale(2, RoundingMode.HALF_UP);
            }

            String displayQtyStr = weightConversionService.formatFromBaseUnit(qtyInBaseUnits, unitCode);

            // --- 7. Restock inventory ---
            product.addStock(qtyInBaseUnits);
            productOutletRepositoryPort.save(product);

            returnItems.add(new ReturnItemDomain(
                    null, product.getId(), qtyInBaseUnits,
                    displayQtyStr, product.getPrice(), subtotal, product.getUnitMeasureId()
            ));

            itemResponses.add(new ReturnItemResponse(
                    product.getId(), product.getName(), displayQtyStr,
                    product.getPrice(), subtotal
            ));
        }

        // --- 8. Create return domain ---
        ReturnDomain returnDomain = ReturnDomain.create(
                originalSale.getId(),
                request.reason(),
                request.notes(),
                request.resolutionType(),
                devStatusId,
                employeeId,
                originalSale.getOutletId(),
                originalSale.getClientId(),
                returnItems
        );

        // --- 9. Apply resolution logic ---
        String resolution = request.resolutionType().toUpperCase();

        MasterRoot origPayMethodNode = masterTree.getById(originalSale.getPaymentMethodId());
        boolean origIsFiado = origPayMethodNode != null && "FIA".equals(origPayMethodNode.getShortName());

        if ("REEMBOLSO".equals(resolution) && origIsFiado) {
            throw new BusinessException(
                    "No se permite reembolso en efectivo de una venta comprada a crédito/fiado (FIA). " +
                    "Seleccione 'Nota de crédito' para reducir la deuda del cliente o 'Cambio por otro producto'.");
        }

        if ("NOTA_CREDITO".equals(resolution)) {
            BigDecimal creditAmount = returnDomain.getTotalRefundAmount();
            final Long creditClientId = clientDomain.getId();
            final Long creditOutletId = originalSale.getOutletId();

            Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
            CreditAccountDomain creditAccount = creditRepositoryPort
                    .findByClientIdAndOutletId(creditClientId, creditOutletId)
                    .orElseGet(() -> {
                        CreditAccountDomain newAcc = CreditAccountDomain.create(
                                creditClientId,
                                creditOutletId,
                                new BigDecimal("150000"),
                                activeStatusId
                        );
                        return creditRepositoryPort.save(newAcc);
                    });

            BigDecimal oldDebt = creditAccount.getCurrentDebt();
            BigDecimal payAmount = creditAmount.min(oldDebt);
            if (payAmount.compareTo(BigDecimal.ZERO) > 0) {
                creditAccount.pay(payAmount);
                creditRepositoryPort.save(creditAccount);
            }

            CreditTransactionDomain txn = CreditTransactionDomain.create(
                    creditAccount.getId(),
                    null,
                    "RETURN_CREDIT",
                    creditAmount,
                    oldDebt,
                    creditAccount.getCurrentDebt(),
                    "Nota de credito por devolucion - Codigo #" + returnDomain.getReturnCode(),
                    employeeId
            );
            creditRepositoryPort.save(txn);
        }

        // --- 10. Save Return ---
        ReturnDomain savedReturn = returnRepositoryPort.save(returnDomain);

        return new ReturnResponse(
                savedReturn.getId(),
                savedReturn.getReturnCode(),
                originalSale.getSaleCode(),
                originalSale.getId(),
                savedReturn.getTotalRefundAmount(),
                savedReturn.getReason(),
                savedReturn.getNotes(),
                savedReturn.getResolutionType(),
                "DEVUELTO",
                clientDomain.getFullName(),
                clientDomain.getNumberid(),
                originalSale.getOutletId(),
                employeeId,
                savedReturn.getReturnDate(),
                itemResponses
        );
    }

    private SaleDomain convertOrderToSaleDomain(OrderDomain order) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot actNode = tree.getByCode("ACT");
        Long activeStatusId = actNode != null ? actNode.getId() : 1L;

        Long cashPaymentMethodId = order.getPaymentMethodId();
        if (cashPaymentMethodId == null) {
            MasterRoot cashNode = tree.getByCode("EFE");
            if (cashNode == null) cashNode = tree.getByCode("MPG_CASH");
            cashPaymentMethodId = cashNode != null ? cashNode.getId() : 1L;
        }

        List<SaleItemDomain> saleItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItemDomain item : order.getItems()) {
                Long pId = item.getProductOutletId();
                if (pId == null) continue;

                ProductDomain product = productOutletRepositoryPort.findById(pId).orElse(null);
                Long unitMeasureId = product != null ? product.getUnitMeasureId() : 1L;
                Integer qty = item.getQuantity() != null ? item.getQuantity() : 1;
                BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : unitPrice.multiply(BigDecimal.valueOf(qty));

                String displayQty = item.getDisplayQuantity();
                if (displayQty == null || displayQty.isBlank()) {
                    MasterRoot unitNode = tree.getById(unitMeasureId);
                    String unitCode = (unitNode != null && unitNode.getShortName() != null) ? unitNode.getShortName() : "UND";
                    displayQty = qty + " " + unitCode;
                }

                saleItems.add(new SaleItemDomain(
                        item.getId(),
                        pId,
                        qty,
                        displayQty,
                        unitPrice,
                        subtotal,
                        unitMeasureId
                ));
            }
        }

        UUID syntheticCode;
        try {
            syntheticCode = UUID.nameUUIDFromBytes(order.getOrderCode().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            syntheticCode = UUID.randomUUID();
        }

        return SaleDomain.fromPersistence(
                order.getId(),
                syntheticCode,
                order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO,
                order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO,
                BigDecimal.ZERO,
                cashPaymentMethodId,
                activeStatusId,
                order.getCustomerId() != null ? order.getCustomerId() : 1L,
                order.getOutletId() != null ? order.getOutletId() : 1L,
                order.getClaimedByUserId() != null ? order.getClaimedByUserId() : 1L,
                order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now(),
                order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now(),
                order.getUpdatedAt() != null ? order.getUpdatedAt() : LocalDateTime.now(),
                saleItems
        );
    }
}
