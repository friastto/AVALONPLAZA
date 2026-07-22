package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
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
 * Lógica de negocio para procesar devoluciones y cambios de productos en POS.
 *
 * Flujo:
 *  1. Validar tenant y rol del empleado
 *  2. Buscar venta original por saleCode (el cliente debe presentar el ticket físico)
 *  3. Validar que los ítems a devolver existan en la venta y la cantidad no supere lo vendido
 *  4. Restituir stock de cada producto devuelto
 *  5. Según resolutionType:
 *     - REEMBOLSO: solo registra la devolución (la caja se ajusta manualmente)
 *     - NOTA_CREDITO: crea transacción de crédito a favor del cliente
 *     - CAMBIO: restituye el stock devuelto (el cambio físico se gestiona en el POS normal)
 *  6. Guardar ReturnDomain
 */
@Service
@RequiredArgsConstructor
public class CreateReturnUseCaseImpl implements CreateReturnUseCase {

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
    public ReturnResponse execute(CreateReturnRequest request) {

        // --- 1. Validar Tenant ---
        UserContext userContext = currentUserProvider.getCurrentUserContext();
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");

        // --- 2. Buscar la Venta Original por saleCode (presentado en el ticket físico) ---
        SaleDomain originalSale = saleRepositoryPort.findByCode(request.originalSaleCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ninguna venta con el código: " + request.originalSaleCode() +
                        ". Verifique que el ticket presentado sea de esta tienda."));

        // Validar que la venta sea de la misma tienda del empleado
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId != null && !tenantOutletId.equals(originalSale.getOutletId())) {
                throw new BusinessException("Acceso denegado: Esta venta pertenece a otra tienda.");
            }
        }

        // --- 3. Resolver empleado ---
        UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        Long employeeId = userDomain.getPersonId();
        if (employeeId == null)
            throw new BusinessException("El usuario actual no tiene un registro de persona (empleado) asociado");

        // --- 4. Buscar cliente de la venta original ---
        PersonDomain clientDomain = personRepositoryPort.findById(originalSale.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente de la venta original no encontrado"));

        // --- 5. Obtener estado DEV para la devolución ---
        Long devStatusId = masterDataRepositoryPort.getIdByCode("DEV");
        if (devStatusId == null) {
            throw new IllegalStateException("Estado 'DEV' (Devuelto) no encontrado en MasterData. Ejecute el INSERT SQL en PostgreSQL.");
        }

        // --- 6. Validar y procesar ítems a devolver ---
        MasterTree masterTree = masterTreeProvider.getTree();
        List<ReturnItemDomain> returnItems = new ArrayList<>();
        List<ReturnItemResponse> itemResponses = new ArrayList<>();

        for (ReturnItemRequest itemReq : request.items()) {

            // Verificar que el producto esté en la venta original
            SaleItemDomain originalItem = originalSale.getItems().stream()
                    .filter(si -> si.getProductId().equals(itemReq.productId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            "El producto con ID " + itemReq.productId() +
                            " no está en la venta original. No se puede devolver."));

            // Obtener el producto del catálogo
            ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto " + itemReq.productId() + " no encontrado"));

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            String unitCode = unitNode != null ? unitNode.getShortName() : "UND";
            boolean isWeighable = weightConversionService.isWeighable(unitCode);

            // Parsear cantidad
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

            if (qtyInBaseUnits <= 0)
                throw new BusinessException("La cantidad a devolver debe ser mayor a cero: " + product.getName());

            // Validar que no supere lo vendido originalmente
            if (qtyInBaseUnits > originalItem.getQuantityInBaseUnits()) {
                throw new BusinessException(
                        "La cantidad a devolver (" + qtyInBaseUnits + ") supera lo vendido (" +
                        originalItem.getQuantityInBaseUnits() + ") para: " + product.getName());
            }

            // Calcular subtotal de devolución
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

            // --- 7. Restituir stock al inventario ---
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

        // --- 8. Crear el dominio de devolución ---
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

        // --- 9. Según resolución, aplicar lógica adicional y validaciones de protección ---
        String resolution = request.resolutionType().toUpperCase();

        MasterRoot origPayMethodNode = masterTree.getById(originalSale.getPaymentMethodId());
        boolean origIsFiado = origPayMethodNode != null && "FIA".equals(origPayMethodNode.getShortName());

        if ("REEMBOLSO".equals(resolution) && origIsFiado) {
            throw new BusinessException(
                    "No se permite reembolso en efectivo de una venta comprada a crédito/fiado (FIA). " +
                    "Seleccione 'Nota de crédito' para reducir la deuda del cliente o 'Cambio por otro producto'.");
        }

        if ("NOTA_CREDITO".equals(resolution)) {
            // Abonar saldo a favor en la cuenta de crédito del cliente
            BigDecimal creditAmount = returnDomain.getTotalRefundAmount();

            Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
            CreditAccountDomain creditAccount = creditRepositoryPort
                    .findByClientIdAndOutletId(clientDomain.getId(), originalSale.getOutletId())
                    .orElseGet(() -> {
                        CreditAccountDomain newAcc = CreditAccountDomain.create(
                                clientDomain.getId(),
                                originalSale.getOutletId(),
                                new BigDecimal("150000"),
                                activeStatusId
                        );
                        return creditRepositoryPort.save(newAcc);
                    });

            BigDecimal oldDebt = creditAccount.getCurrentDebt();
            // Aplicar nota de crédito: reducir la deuda solo hasta 0 (no puede quedar negativa)
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
                    "Nota de crédito por devolución - Código #" + returnDomain.getReturnCode(),
                    employeeId
            );
            creditRepositoryPort.save(txn);
        }
        // REEMBOLSO y CAMBIO: el stock ya fue restituido arriba.
        // REEMBOLSO: el empleado devuelve el dinero físicamente en caja.
        // CAMBIO: el cliente selecciona otro producto en el POS normal.

        // --- 10. Guardar Devolución ---
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
                "DEV",
                clientDomain.getFullName(),
                clientDomain.getNumberid(),
                savedReturn.getOutletId(),
                savedReturn.getEmployeeId(),
                savedReturn.getReturnDate(),
                itemResponses
        );
    }
}
