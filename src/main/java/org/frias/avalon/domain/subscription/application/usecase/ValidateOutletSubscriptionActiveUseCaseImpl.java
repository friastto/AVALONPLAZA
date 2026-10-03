package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.core.exeptions.SubscriptionSuspendedException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of ValidateOutletSubscriptionActiveUseCase.
 * Protects mutator business operations (POS sales, open cash register, orders).
 */
@Service
public class ValidateOutletSubscriptionActiveUseCaseImpl implements ValidateOutletSubscriptionActiveUseCase {

    private final OutletSubscriptionRepositoryPort subscriptionPort;
    private final MasterTreeProvider masterTreeProvider;

    public ValidateOutletSubscriptionActiveUseCaseImpl(
            OutletSubscriptionRepositoryPort subscriptionPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.subscriptionPort = subscriptionPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public void execute(Long outletId) {
        if (outletId == null) {
            return;
        }

        OutletSubscriptionDomain sub = subscriptionPort.findByOutletId(outletId).orElse(null);
        if (sub == null) {
            // New or uninitialized store is permitted to operate
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot statusNode = tree.getById(sub.statusId());

        boolean isSuspendedByDate = now.isAfter(sub.gracePeriodEnd());
        boolean isSuspendedByStatus = tree.is(statusNode, "SUB_SUSP") || tree.is(statusNode, "SUB_CANC");

        if (isSuspendedByDate || isSuspendedByStatus) {
            throw new SubscriptionSuspendedException(
                    "Operacion no permitida. La suscripcion de esta tienda se encuentra suspendida por pago pendiente tras agotar el periodo de gracia. Modo solo lectura activo."
            );
        }
    }
}
