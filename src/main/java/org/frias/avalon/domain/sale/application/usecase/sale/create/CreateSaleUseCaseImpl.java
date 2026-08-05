package org.frias.avalon.domain.sale.application.usecase.sale.create;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
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
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.request.SaleItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateSaleUseCaseImpl implements CreateSaleUseCase {

    private final SaleRepositoryPort saleRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SaleWeightConversionService weightConversionService;
    private final CurrentUserProviderPort currentUserProvider;
    private final CreditRepositoryPort creditRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {
        // --- 1. Validar Encapsulación de Tienda (Tenant Isolation) ---
        UserContext userContext = currentUserProvider.getCurrentUserContext();
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            if (!tenantOutletId.equals(request.outletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para registrar ventas en otra tienda.");
            }
        }

        // --- 2. Validar Rol Operativo/Ventas usando el MasterTree ---
        MasterTree masterTree = masterTreeProvider.getTree();
        boolean hasAuthorizedRole = false;
        
        for (String roleName : userContext.roles()) {
            // Limpiar prefijo ROLE_ si está presente
            String shortCode = roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
            MasterRoot roleNode = masterTree.getByCode(shortCode);
            if (roleNode != null) {
                // Verificar si es descendiente o igual a OPERACION, GESTION o ADMINSYS
                if (masterTree.isChildOf(roleNode, "OPT") ||
                    masterTree.isChildOf(roleNode, "GESTION") || 
                    masterTree.isChildOf(roleNode, "ADMINSYS") ||
                    shortCode.equals("OPERACION") || 
                    shortCode.equals("GESTION") ||
                    shortCode.equals("ADMINSYS") ||
                    shortCode.equals("ADMIN") ||
                    shortCode.equals("DUENO")) {
                    hasAuthorizedRole = true;
                    break;
                }
            }
        }
        if (!hasAuthorizedRole && !isSystemAdmin) {
            throw new BusinessException("Acceso denegado: Tu rol actual no tiene autorización para registrar ventas.");
        }

        // --- 3. Resolver Empleado ---
        UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe en el sistema"));
        Long employeeId = userDomain.getPersonId();
        if (employeeId == null) {
            throw new BusinessException("El usuario actual no tiene un registro de persona (empleado) asociado");
        }

        // --- 4. Resolver Cliente ---
        PersonDomain clientDomain = personRepositoryPort.findByNumberid(request.clientNumberid())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con identificación '" + request.clientNumberid() + "' no encontrado."));

        // --- 5. Obtener Estado de la Venta (ACT) ---
        Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        if (activeStatusId == null) {
            throw new IllegalStateException("Estado Activo ('ACT') no encontrado en MasterData.");
        }

        // --- 6. Procesar Items y Descontar Inventario ---
        List<SaleItemDomain> itemDomains = new ArrayList<>();
        List<SaleItemResponse> itemResponses = new ArrayList<>();

        for (SaleItemRequest itemReq : request.items()) {
            ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + itemReq.productId() + " no existe"));

            // Validar que el producto sea de la misma tienda de la venta
            if (!product.getOutletId().equals(request.outletId())) {
                throw new BusinessException("El producto '" + product.getName() + "' no pertenece a la tienda de la venta.");
            }

            MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
            if (unitNode == null) {
                throw new DomainValidationException("La unidad de medida del producto " + product.getName() + " no es válida.");
            }
            String unitCode = unitNode.getShortName();

            // Parsea la cantidad y convierte a la unidad base
            Integer qtyInBaseUnits;
            BigDecimal displayQty;
            
            boolean isWeighable = weightConversionService.isWeighable(unitCode);
            if (isWeighable) {
                // Cantidad decimal permitida
                try {
                    String cleanQty = itemReq.quantity().replace(",", ".");
                    displayQty = new BigDecimal(cleanQty);
                } catch (NumberFormatException e) {
                    throw new BusinessException("La cantidad '" + itemReq.quantity() + "' no es un decimal válido para el producto pesable: " + product.getName());
                }
                qtyInBaseUnits = weightConversionService.convertToBaseUnit(displayQty, unitCode);
            } else {
                // Cantidad debe ser un entero
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

            // Descontar Stock del producto
            product.removeStock(qtyInBaseUnits);
            productOutletRepositoryPort.save(product);

            // Calcular subtotal
            BigDecimal subtotal;
            if (itemReq.customLineTotal() != null && itemReq.customLineTotal().compareTo(BigDecimal.ZERO) > 0) {
                subtotal = itemReq.customLineTotal().setScale(2, RoundingMode.HALF_UP);
            } else if (isWeighable) {
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

            SaleItemDomain itemDomain = new SaleItemDomain(
                    null,
                    product.getId(),
                    qtyInBaseUnits,
                    displayQtyStr,
                    product.getPrice(),
                    subtotal,
                    product.getUnitMeasureId()
            );
            itemDomains.add(itemDomain);

            itemResponses.add(new SaleItemResponse(
                    product.getId(),
                    product.getName(),
                    displayQtyStr,
                    product.getPrice(),
                    subtotal
            ));
        }

        // --- 7. Crear la Venta ---
        SaleDomain saleDomain = SaleDomain.create(
                request.paymentMethodId(),
                activeStatusId,
                clientDomain.getId(),
                request.outletId(),
                employeeId,
                itemDomains
        );

        // --- 8. Aplicar Pago si viene recibido ---
        if (request.amountReceived() != null) {
            MasterRoot payMethodNode = masterTreeProvider.getTree().getById(request.paymentMethodId());
            boolean isFiado = payMethodNode != null && "FIA".equals(payMethodNode.getShortName());
            saleDomain.applyPayment(request.amountReceived(), isFiado);
        }

        // --- 9. Guardar la Venta ---
        SaleDomain savedSale = saleRepositoryPort.save(saleDomain);

        // --- 9.1. Si el método de pago es FIADO (FIA), aplicar cargos a la cuenta ---
        MasterRoot payMethodNode = masterTree.getById(savedSale.getPaymentMethodId());
        if (payMethodNode != null && "FIA".equals(payMethodNode.getShortName())) {
            BigDecimal totalAmount = savedSale.getTotalAmount();
            
            // Buscar o crear la cuenta de crédito del cliente
            CreditAccountDomain creditAccount = creditRepositoryPort.findByClientIdAndOutletId(clientDomain.getId(), request.outletId())
                    .orElseGet(() -> {
                        CreditAccountDomain newAcc = CreditAccountDomain.create(
                                clientDomain.getId(),
                                request.outletId(),
                                new BigDecimal("150000"), // Límite por defecto
                                activeStatusId
                        );
                        return creditRepositoryPort.save(newAcc);
                    });

            BigDecimal oldDebt = creditAccount.getCurrentDebt();
            creditAccount.charge(totalAmount);
            creditRepositoryPort.save(creditAccount);

            // Registrar la transacción de compra fiada
            CreditTransactionDomain txn = CreditTransactionDomain.create(
                    creditAccount.getId(),
                    savedSale.getId(),
                    "PURCHASE",
                    totalAmount,
                    oldDebt,
                    creditAccount.getCurrentDebt(),
                    "Compra fiada en POS - Ticket #" + savedSale.getSaleCode(),
                    employeeId
            );
            creditRepositoryPort.save(txn);
        }

        // --- 10. Mapear y Retornar la Respuesta Expandida ---
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

        SaleResponse response = new SaleResponse(
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
                itemResponses
        );

        String emailToSend = Boolean.TRUE.equals(request.sendEmail()) ? clientDomain.getEmail() : null;
        eventPublisher.publishEvent(new SaleCreatedEvent(this, response, emailToSend));

        return response;
    }
}
