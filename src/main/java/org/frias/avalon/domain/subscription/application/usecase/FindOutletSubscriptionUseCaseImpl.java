package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.subscription.application.dto.response.OutletSubscriptionResponseDto;
import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.frias.avalon.domain.subscription.domain.service.BillingCycleCalculatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class FindOutletSubscriptionUseCaseImpl implements FindOutletSubscriptionUseCase {

    private final OutletRepositoryPort outletPort;
    private final OutletSubscriptionRepositoryPort subscriptionPort;
    private final BillingCycleCalculatorService billingCycleCalculatorService;
    private final MasterTreeProvider masterTreeProvider;

    public FindOutletSubscriptionUseCaseImpl(
            OutletRepositoryPort outletPort,
            OutletSubscriptionRepositoryPort subscriptionPort,
            BillingCycleCalculatorService billingCycleCalculatorService,
            MasterTreeProvider masterTreeProvider
    ) {
        this.outletPort = outletPort;
        this.subscriptionPort = subscriptionPort;
        this.billingCycleCalculatorService = billingCycleCalculatorService;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public OutletSubscriptionResponseDto execute(Long outletId) {
        if (outletId == null) {
            throw new IllegalArgumentException("El identificador de tienda no puede ser nulo");
        }

        OutletDomain outlet = outletPort.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada con id: " + outletId));

        MasterTree tree = masterTreeProvider.getTree();
        LocalDateTime now = LocalDateTime.now();

        OutletSubscriptionDomain sub = subscriptionPort.findByOutletId(outletId)
                .orElseGet(() -> createInitialSubscription(outlet, now, tree));

        long daysUntilDue = ChronoUnit.DAYS.between(now.toLocalDate(), sub.currentPeriodEnd().toLocalDate());
        boolean isDueSoon = daysUntilDue <= 3 && !now.isAfter(sub.currentPeriodEnd());
        boolean isInGracePeriod = now.isAfter(sub.currentPeriodEnd()) && !now.isAfter(sub.gracePeriodEnd());
        boolean isSuspended = now.isAfter(sub.gracePeriodEnd());
        boolean canOperate = !isSuspended;

        String statusCode;
        if (isSuspended) {
            statusCode = "SUB_SUSP";
        } else if (isInGracePeriod) {
            statusCode = "SUB_GRACE";
        } else if (now.isBefore(sub.trialEndsAt())) {
            statusCode = "SUB_TRIAL";
        } else {
            statusCode = "SUB_ACT";
        }

        MasterRoot statusNode = tree.getByCode(statusCode);
        if (statusNode != null && !statusNode.getId().equals(sub.statusId())) {
            sub = new OutletSubscriptionDomain(
                    sub.id(),
                    sub.outletId(),
                    sub.companyId(),
                    statusNode.getId(),
                    sub.billingDay(),
                    sub.amountCop(),
                    sub.trialEndsAt(),
                    sub.currentPeriodStart(),
                    sub.currentPeriodEnd(),
                    sub.gracePeriodEnd(),
                    sub.createdAt(),
                    now
            );
            subscriptionPort.save(sub);
        }

        MasterRefDto statusRef = statusNode != null ? MasterRefDto.from(statusNode) : null;

        return new OutletSubscriptionResponseDto(
                sub.id(),
                outlet.getId(),
                outlet.getName(),
                sub.companyId(),
                statusRef,
                sub.billingDay(),
                sub.amountCop(),
                sub.trialEndsAt(),
                sub.currentPeriodStart(),
                sub.currentPeriodEnd(),
                sub.gracePeriodEnd(),
                Math.max(0, daysUntilDue),
                isDueSoon,
                isInGracePeriod,
                isSuspended,
                canOperate
        );
    }

    private OutletSubscriptionDomain createInitialSubscription(OutletDomain outlet, LocalDateTime now, MasterTree tree) {
        var schedule = billingCycleCalculatorService.calculateInitialSchedule(
                now,
                BillingCycleCalculatorService.DEFAULT_BILLING_DAY
        );

        MasterRoot subTrial = tree.getByCode("SUB_TRIAL");
        if (subTrial == null) {
            throw new IllegalStateException("MasterData node SUB_TRIAL not found in MasterTree");
        }
        Long statusId = subTrial.getId();

        OutletSubscriptionDomain newSub = new OutletSubscriptionDomain(
                null,
                outlet.getId(),
                outlet.getCompanyId(),
                statusId,
                BillingCycleCalculatorService.DEFAULT_BILLING_DAY,
                new BigDecimal("60000.00"),
                schedule.trialEndsAt(),
                schedule.currentPeriodStart(),
                schedule.currentPeriodEnd(),
                schedule.gracePeriodEnd(),
                now,
                now
        );
        return subscriptionPort.save(newSub);
    }
}
