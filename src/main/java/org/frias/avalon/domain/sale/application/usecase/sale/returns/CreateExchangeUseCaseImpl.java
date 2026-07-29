package org.frias.avalon.domain.sale.application.usecase.sale.returns;

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
import org.frias.avalon.domain.sale.application.dto.request.*;
import org.frias.avalon.domain.sale.application.dto.response.*;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.*;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para intercambios/cambios de producto con cálculo de excedente.
 *
 * Flujo:
 *  1. Reintegra stock de ítems devueltos.
 *  2. Descuenta stock de los nuevos productos de reemplazo.
 *  3. Calcula netDifference = totalNuevos - totalDevueltos.
 *  4. Si netDifference > 0 (excedente):
 *     - Si paymentMethod es FIA: suma netDifference a la deuda de fiado del cliente.
 *     - Si es Contado: cobra netDifference en efectivo/digital.
 *  5. Si netDifference < 0 (sobrante): reduce la deuda activa del cliente o genera saldo.
 */
@Service
@RequiredArgsConstructor
public class CreateExchangeUseCaseImpl implements CreateExchangeUseCase {

    private final ReturnRepositoryPort returnRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
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
    public ExchangeResponse execute(CreateExchangeRequest request) {

        UserContext userContext = currentUserProvider.getCurrentUserContext();
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");

        // --- 1. Venta Original ---
        SaleDomain originalSale = saleRepositoryPort.findByCode(request.originalSaleCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la venta original con código: " + request.originalSaleCode()));

        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId != null && !tenantOutletId.equals(originalSale.getOutletId())) {
                throw new BusinessException("Acceso denegado: La venta pertenece a otra tienda.");
            }
        }

        // --- 2. Empleado y Cliente ---
        UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        Long employeeId = userDomain.getPersonId();
        if (employeeId == null)
            throw new BusinessException("El usuario actual no tiene un registro de empleado asociado.");

        PersonDomain clientDomain = personRepositoryPort.findById(originalSale.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente de la venta original no encontrado"));

        MasterTree masterTree = masterTreeProvider.getTree();
        Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        Long devStatusId = masterDataRepositoryPort.getIdByCode("DEV");
        if (devStatusId == null) {
            throw new IllegalStateException("Estado 'DEV' (Devuelto) no encontrado en MasterData.");
        }

        // --- 3. Procesar Productos Devueltos (Reintegrar Stock) ---
        List<ReturnItemDomain> returnItems = new ArrayList<>();
        List<ReturnItemResponse> returnItemResponses = new ArrayList<>();
        BigDecimal totalReturned = BigDecimal.ZERO;

        for (ReturnItemRequest itemReq : request.returnedItems()) {
            SaleItemDomain originalItem = originalSale.getItems().stream()
                    .filter(si -> si.getProductId().equals(itemReq.productId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("El producto con ID " + itemReq.productId() + " no está en la venta original."));

            ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto " + itemReq.productId() + " no encontrado"));

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            String unitCode = unitNode != null ? unitNode.getShortName() : "UND";
            boolean isWeighable = weightConversionService.isWeighable(unitCode);

            Integer qtyInBaseUnits;
            BigDecimal displayQty;
            if (isWeighable) {
                try {
                    displayQty = new BigDecimal(itemReq.quantity().replace(",", "."));
                } catch (Exception e) {
                    throw new BusinessException("Cantidad decimal inválida para producto pesable: " + product.getName());
                }
                qtyInBaseUnits = weightConversionService.convertToBaseUnit(displayQty, unitCode);
            } else {
                try {
                    qtyInBaseUnits = Integer.parseInt(itemReq.quantity());
                    displayQty = new BigDecimal(qtyInBaseUnits);
                } catch (Exception e) {
                    throw new BusinessException("La cantidad debe ser un entero para: " + product.getName());
                }
            }

            if (qtyInBaseUnits <= 0)
                throw new BusinessException("La cantidad a devolver debe ser mayor a cero: " + product.getName());

            if (qtyInBaseUnits > originalItem.getQuantityInBaseUnits())
                throw new BusinessException("La cantidad a devolver supera la vendida originalmente para: " + product.getName());

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

            totalReturned = totalReturned.add(subtotal);

            // Restituir stock
            product.addStock(qtyInBaseUnits);
            productOutletRepositoryPort.save(product);

            String displayQtyStr = weightConversionService.formatFromBaseUnit(qtyInBaseUnits, unitCode);
            returnItems.add(new ReturnItemDomain(null, product.getId(), qtyInBaseUnits, displayQtyStr, product.getPrice(), subtotal, product.getUnitMeasureId()));
            returnItemResponses.add(new ReturnItemResponse(product.getId(), product.getName(), displayQtyStr, product.getPrice(), subtotal));
        }

        ReturnDomain returnDomain = ReturnDomain.create(
                originalSale.getId(), request.reason(), request.notes(), "CAMBIO",
                devStatusId, employeeId, originalSale.getOutletId(), clientDomain.getId(), returnItems
        );
        ReturnDomain savedReturn = returnRepositoryPort.save(returnDomain);

        ReturnResponse returnResponse = new ReturnResponse(
                savedReturn.getId(), savedReturn.getReturnCode(), originalSale.getSaleCode(), originalSale.getId(),
                savedReturn.getTotalRefundAmount(), savedReturn.getReason(), savedReturn.getNotes(), savedReturn.getResolutionType(),
                "DEV", clientDomain.getFullName(), clientDomain.getNumberid(),
                savedReturn.getOutletId(), savedReturn.getEmployeeId(), savedReturn.getReturnDate(), returnItemResponses
        );

        // --- 4. Procesar Productos de Reemplazo (Descontar Stock) ---
        List<SaleItemDomain> newSaleItems = new ArrayList<>();
        List<SaleItemResponse> newSaleItemResponses = new ArrayList<>();
        BigDecimal totalNewItems = BigDecimal.ZERO;

        for (ExchangeItemRequest exReq : request.exchangeItems()) {
            ProductDomain product = productOutletRepositoryPort.findById(exReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto de reemplazo " + exReq.productId() + " no encontrado"));

            if (!product.getOutletId().equals(originalSale.getOutletId())) {
                throw new BusinessException("El producto '" + product.getName() + "' pertenece a otra tienda.");
            }

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            String unitCode = unitNode != null ? unitNode.getShortName() : "UND";
            boolean isWeighable = weightConversionService.isWeighable(unitCode);

            Integer qtyInBaseUnits;
            BigDecimal displayQty;
            if (isWeighable) {
                try {
                    displayQty = new BigDecimal(exReq.quantity().replace(",", "."));
                } catch (Exception e) {
                    throw new BusinessException("Cantidad inválida para producto pesable: " + product.getName());
                }
                qtyInBaseUnits = weightConversionService.convertToBaseUnit(displayQty, unitCode);
            } else {
                try {
                    qtyInBaseUnits = Integer.parseInt(exReq.quantity());
                    displayQty = new BigDecimal(qtyInBaseUnits);
                } catch (Exception e) {
                    throw new BusinessException("La cantidad debe ser entero para: " + product.getName());
                }
            }

            if (qtyInBaseUnits <= 0)
                throw new BusinessException("La cantidad del producto nuevo debe ser mayor a cero: " + product.getName());

            // Descontar stock
            product.removeStock(qtyInBaseUnits);
            productOutletRepositoryPort.save(product);

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

            totalNewItems = totalNewItems.add(subtotal);

            String displayQtyStr = weightConversionService.formatFromBaseUnit(qtyInBaseUnits, unitCode);
            newSaleItems.add(new SaleItemDomain(null, product.getId(), qtyInBaseUnits, displayQtyStr, product.getPrice(), subtotal, product.getUnitMeasureId()));
            newSaleItemResponses.add(new SaleItemResponse(product.getId(), product.getName(), displayQtyStr, product.getPrice(), subtotal));
        }

        // --- 5. Calcular Diferencia Net ---
        BigDecimal netDifference = totalNewItems.subtract(totalReturned);
        String paymentStatusMsg;

        MasterRoot payMethodNode = masterTree.getById(request.paymentMethodId());
        boolean isFiado = payMethodNode != null && "FIA".equals(payMethodNode.getShortName());

        SaleDomain newSaleDomain = SaleDomain.create(
                request.paymentMethodId(), activeStatusId, clientDomain.getId(),
                originalSale.getOutletId(), employeeId, newSaleItems
        );

        if (netDifference.compareTo(BigDecimal.ZERO) > 0) {
            // El cliente debe pagar la diferencia (Excedente)
            if (isFiado) {
                // Sumar excedente a la libreta de fiado
                CreditAccountDomain creditAccount = creditRepositoryPort.findByClientIdAndOutletId(clientDomain.getId(), originalSale.getOutletId())
                        .orElseGet(() -> creditRepositoryPort.save(CreditAccountDomain.create(clientDomain.getId(), originalSale.getOutletId(), new BigDecimal("150000"), activeStatusId)));

                BigDecimal oldDebt = creditAccount.getCurrentDebt();
                creditAccount.charge(netDifference);
                creditRepositoryPort.save(creditAccount);

                CreditTransactionDomain txn = CreditTransactionDomain.create(
                        creditAccount.getId(), null, "PURCHASE", netDifference,
                        oldDebt, creditAccount.getCurrentDebt(),
                        "Excedente de cambio de producto - Ref ticket #" + originalSale.getSaleCode(),
                        employeeId
                );
                creditRepositoryPort.save(txn);

                newSaleDomain.applyPayment(BigDecimal.ZERO, true);
                paymentStatusMsg = "Excedente de $" + netDifference + " cargado a la libreta de fiado del cliente.";
            } else {
                // Pago en efectivo / digital
                BigDecimal received = request.amountReceived() != null ? request.amountReceived() : netDifference;
                newSaleDomain.applyPayment(received, false);
                paymentStatusMsg = "Excedente de $" + netDifference + " cobrado exitosamente.";
            }
        } else if (netDifference.compareTo(BigDecimal.ZERO) < 0) {
            // El nuevo producto es más barato (saldo a favor del cliente)
            BigDecimal surplus = netDifference.abs();
            newSaleDomain.applyPayment(BigDecimal.ZERO, false);

            CreditAccountDomain creditAccount = creditRepositoryPort.findByClientIdAndOutletId(clientDomain.getId(), originalSale.getOutletId()).orElse(null);
            if (creditAccount != null && creditAccount.getCurrentDebt().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal oldDebt = creditAccount.getCurrentDebt();
                BigDecimal payAmount = surplus.min(oldDebt);
                creditAccount.pay(payAmount);
                creditRepositoryPort.save(creditAccount);

                CreditTransactionDomain txn = CreditTransactionDomain.create(
                        creditAccount.getId(), null, "RETURN_CREDIT", surplus,
                        oldDebt, creditAccount.getCurrentDebt(),
                        "Abono por diferencia a favor en cambio de producto", employeeId
                );
                creditRepositoryPort.save(txn);
                paymentStatusMsg = "Diferencia a favor de $" + surplus + " aplicada para abonar a la deuda del cliente.";
            } else {
                paymentStatusMsg = "Diferencia a favor de $" + surplus + " entregada al cliente.";
            }
        } else {
            // Cambio de valor exacto
            newSaleDomain.applyPayment(BigDecimal.ZERO, false);
            paymentStatusMsg = "Cambio realizado de igual valor. Sin saldos pendientes.";
        }

        SaleDomain savedNewSale = saleRepositoryPort.save(newSaleDomain);

        MasterDataResponseDto payDto = new MasterDataResponseDto(payMethodNode.getId(), payMethodNode.getShortName(), payMethodNode.getFullName());
        MasterRoot statusNode = masterTree.getById(savedNewSale.getStatusId());
        MasterDataResponseDto statusDto = new MasterDataResponseDto(statusNode.getId(), statusNode.getShortName(), statusNode.getFullName());

        SaleResponse newSaleResponse = new SaleResponse(
                savedNewSale.getId(), savedNewSale.getSaleCode(), savedNewSale.getTotalAmount(),
                savedNewSale.getAmountReceived(), savedNewSale.getChangeGiven(), savedNewSale.getSaleDate(),
                payDto, statusDto, clientDomain.getFullName(), clientDomain.getNumberid(),
                savedNewSale.getOutletId(), savedNewSale.getEmployeeId(), newSaleItemResponses
        );

        return new ExchangeResponse(
                returnResponse, newSaleResponse, totalReturned, totalNewItems, netDifference, paymentStatusMsg
        );
    }
}
