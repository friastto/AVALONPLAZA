package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateSaleUseCaseImpl implements CreateSaleUseCase {

    private final SaleRepositoryPort saleRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SaleWeightConversionService weightConversionService;
    private final CurrentUserProviderPort currentUserProvider;
    private final CreditRepositoryPort creditRepositoryPort;
    private final ApplicationEventPublisher eventPublisher;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public CreateSaleUseCaseImpl(
            SaleRepositoryPort saleRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            SaleWeightConversionService weightConversionService,
            CurrentUserProviderPort currentUserProvider,
            CreditRepositoryPort creditRepositoryPort,
            ApplicationEventPublisher eventPublisher,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager) {
        this.saleRepositoryPort = saleRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.weightConversionService = weightConversionService;
        this.currentUserProvider = currentUserProvider;
        this.creditRepositoryPort = creditRepositoryPort;
        this.eventPublisher = eventPublisher;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public SaleResponse execute(CreateSaleRequest request) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            // --- 1. Resolver Tienda y Validar Encapsulacion de Tienda (Tenant Isolation & RBAC) ---
            OutletDomain outlet = outletRepositoryPort.findById(request.outletId())
                    .orElseThrow(() -> new ResourceNotFoundException("La tienda con ID " + request.outletId() + " no existe."));

            UserContext userContext = currentUserProvider.getCurrentUserContext();
            boolean isGlobalAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
            boolean isCompanyAdmin = currentUserProvider.hasRole("ROLE_GERGEN");
            Long userOutletId = currentUserProvider.getCurrentOutletId();
            Long userTenantId = currentUserProvider.getCurrentTenantId();

            if (!isGlobalAdmin) {
                if (isCompanyAdmin) {
                    if (userTenantId != null && !userTenantId.equals(outlet.getCompanyId())) {
                        throw new BusinessException("Acceso denegado: La tienda no pertenece a tu compania.");
                    }
                } else {
                    if (userOutletId == null) {
                        throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
                    }
                    if (!userOutletId.equals(request.outletId())) {
                        throw new BusinessException("Acceso denegado: No tienes permisos para registrar ventas en otra tienda.");
                    }
                }
            }

            // --- 2. Validar Rol Operativo/Ventas usando el MasterTree ---
            MasterTree masterTree = masterTreeProvider.getTree();
            boolean hasAuthorizedRole = false;

            if (userContext != null && userContext.roles() != null) {
                for (String roleName : userContext.roles()) {
                    String shortCode = roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
                    MasterRoot roleNode = masterTree.getByCode(shortCode);
                    if (roleNode != null) {
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
            }

            if (!hasAuthorizedRole && !isGlobalAdmin) {
                throw new BusinessException("Acceso denegado: Tu rol actual no tiene autorización para registrar ventas.");
            }

            // --- 3. Conmutar TenantContext ---
            if (outlet.getCompanyId() != null) {
                TenantContext.setTenantId(outlet.getCompanyId());
            }
            TenantContext.setTenantOutletId(outlet.getId());

            // --- 4. Ejecutar transaccion en esquema de la tienda ---
            return transactionTemplate.execute(status -> {
                String username = userContext != null ? userContext.username() : null;
                UserAvalonDomain userDomain = userAvalonRepositoryPort.findByUserName(username)
                        .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe en el sistema"));
                Long employeeId = userDomain.getPersonId();
                if (employeeId == null) {
                    throw new BusinessException("El usuario actual no tiene un registro de persona (empleado) asociado");
                }

                PersonDomain clientDomain = personRepositoryPort.findByNumberid(request.clientNumberid())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente con identificación '" + request.clientNumberid() + "' no encontrado."));

                MasterRoot actNode = masterTree.getByCode("ACT");
                if (actNode == null) {
                    throw new IllegalStateException("Estado Activo ('ACT') no encontrado en MasterData.");
                }
                Long activeStatusId = actNode.getId();

                Long paymentMethodId = request.paymentMethodId();
                if (paymentMethodId == null && request.paymentMethodCode() != null && !request.paymentMethodCode().isBlank()) {
                    MasterRoot payNode = masterTree.getByCode(request.paymentMethodCode().trim());
                    if (payNode != null) {
                        paymentMethodId = payNode.getId();
                    }
                }
                if (paymentMethodId == null) {
                    throw new DomainValidationException("El método de pago es requerido (suministre paymentMethodId o paymentMethodCode).");
                }

                List<SaleItemDomain> itemDomains = new ArrayList<>();
                List<SaleItemResponse> itemResponses = new ArrayList<>();

                for (SaleItemRequest itemReq : request.items()) {
                    ProductDomain product = productOutletRepositoryPort.findById(itemReq.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + itemReq.productId() + " no existe"));

                    if (!product.getOutletId().equals(request.outletId())) {
                        throw new BusinessException("El producto '" + product.getName() + "' no pertenece a la tienda de la venta.");
                    }

                    MasterRoot unitNode = masterTree.getById(product.getUnitMeasureId());
                    if (unitNode == null) {
                        throw new DomainValidationException("La unidad de medida del producto " + product.getName() + " no es válida.");
                    }
                    String unitCode = unitNode.getShortName();

                    Integer qtyInBaseUnits;
                    BigDecimal displayQty;

                    boolean isWeighable = weightConversionService.isWeighable(unitCode);
                    if (isWeighable) {
                        try {
                            String cleanQty = itemReq.quantity().replace(",", ".");
                            displayQty = new BigDecimal(cleanQty);
                        } catch (NumberFormatException e) {
                            throw new BusinessException("La cantidad '" + itemReq.quantity() + "' no es un decimal válido para el producto pesable: " + product.getName());
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

                    product.removeStock(qtyInBaseUnits);
                    productOutletRepositoryPort.save(product);

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

                SaleDomain saleDomain = SaleDomain.create(
                        paymentMethodId,
                        activeStatusId,
                        clientDomain.getId(),
                        request.outletId(),
                        employeeId,
                        itemDomains
                );

                if (request.amountReceived() != null) {
                    MasterRoot payMethodNode = masterTree.getById(paymentMethodId);
                    boolean isFiado = payMethodNode != null && "FIA".equals(payMethodNode.getShortName());
                    saleDomain.applyPayment(request.amountReceived(), isFiado);
                }

                SaleDomain savedSale = saleRepositoryPort.save(saleDomain);

                MasterRoot payMethodNode = masterTree.getById(savedSale.getPaymentMethodId());
                if (payMethodNode != null && "FIA".equals(payMethodNode.getShortName())) {
                    BigDecimal totalAmount = savedSale.getTotalAmount();

                    CreditAccountDomain creditAccount = creditRepositoryPort.findByClientIdAndOutletId(clientDomain.getId(), request.outletId())
                            .orElseGet(() -> {
                                CreditAccountDomain newAcc = CreditAccountDomain.create(
                                        clientDomain.getId(),
                                        request.outletId(),
                                        new BigDecimal("150000"),
                                        activeStatusId
                                );
                                return creditRepositoryPort.save(newAcc);
                            });

                    BigDecimal oldDebt = creditAccount.getCurrentDebt();
                    creditAccount.charge(totalAmount);
                    creditRepositoryPort.save(creditAccount);

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
            });
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
