package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.subscription.application.dto.response.OutletCheckoutSessionResponseDto;
import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.model.SubscriptionPaymentDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.frias.avalon.domain.subscription.domain.port.SubscriptionPaymentRepositoryPort;
import org.frias.avalon.domain.subscription.domain.service.BillingCycleCalculatorService;
import org.frias.avalon.domain.subscription.domain.service.WompiSecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation of CreateOutletCheckoutSessionUseCase.
 */
@Service
public class CreateOutletCheckoutSessionUseCaseImpl implements CreateOutletCheckoutSessionUseCase {

    private final OutletSubscriptionRepositoryPort subscriptionPort;
    private final SubscriptionPaymentRepositoryPort paymentPort;
    private final OutletRepositoryPort outletPort;
    private final CompanyRepositoryPort companyPort;
    private final WompiSecurityService wompiSecurityService;
    private final BillingCycleCalculatorService billingCycleCalculatorService;
    private final MasterTreeProvider masterTreeProvider;
    private final String publicKey;
    private final BigDecimal defaultMonthlyAmount;

    public CreateOutletCheckoutSessionUseCaseImpl(
            OutletSubscriptionRepositoryPort subscriptionPort,
            SubscriptionPaymentRepositoryPort paymentPort,
            OutletRepositoryPort outletPort,
            CompanyRepositoryPort companyPort,
            WompiSecurityService wompiSecurityService,
            BillingCycleCalculatorService billingCycleCalculatorService,
            MasterTreeProvider masterTreeProvider,
            @Value("${wompi.public-key:pub_test_dummy}") String publicKey,
            @Value("${wompi.monthly-amount-cop:60000.00}") BigDecimal defaultMonthlyAmount
    ) {
        this.subscriptionPort = subscriptionPort;
        this.paymentPort = paymentPort;
        this.outletPort = outletPort;
        this.companyPort = companyPort;
        this.wompiSecurityService = wompiSecurityService;
        this.billingCycleCalculatorService = billingCycleCalculatorService;
        this.masterTreeProvider = masterTreeProvider;
        this.publicKey = publicKey;
        this.defaultMonthlyAmount = defaultMonthlyAmount;
    }

    @Override
    @Transactional
    public OutletCheckoutSessionResponseDto execute(Long outletId) {
        OutletDomain outlet = outletPort.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Outlet not found with id: " + outletId));

        MasterTree tree = masterTreeProvider.getTree();
        LocalDateTime now = LocalDateTime.now();

        OutletSubscriptionDomain subscription = subscriptionPort.findByOutletId(outletId)
                .orElseGet(() -> createInitialSubscription(outlet, now, tree));

        BigDecimal amountCop = subscription.amountCop() != null ? subscription.amountCop() : defaultMonthlyAmount;
        long amountInCents = amountCop.multiply(new BigDecimal(100)).longValue();
        String currency = "COP";
        String storeCode = (outlet.getCode() != null && !outlet.getCode().isBlank())
                ? outlet.getCode().trim().replaceAll("[^a-zA-Z0-9]", "")
                : ("OUT" + outletId);
        String storeNit = (outlet.getNit() != null && !outlet.getNit().isBlank())
                ? outlet.getNit().trim().replaceAll("[^a-zA-Z0-9]", "")
                : "0";
        String reference = "SUB-" + storeCode + "-" + storeNit + "-" + System.currentTimeMillis();

        String signature = wompiSecurityService.generateIntegritySignature(reference, amountInCents, currency);

        SubscriptionPaymentDomain pendingPayment = new SubscriptionPaymentDomain(
                null,
                subscription.id(),
                outletId,
                subscription.companyId(),
                null,
                reference,
                null,
                amountInCents,
                currency,
                "PENDING",
                signature,
                null,
                now,
                now
        );
        paymentPort.save(pendingPayment);

        String companyEmail = companyPort.findById(subscription.companyId())
                .map(CompanyDomain::email)
                .orElse(null);

        String checkoutUrl = "https://checkout.wompi.co/p/?public-key=" + publicKey
                + "&currency=" + currency
                + "&amount-in-cents=" + amountInCents
                + "&reference=" + reference
                + "&signature:integrity=" + signature;

        return new OutletCheckoutSessionResponseDto(
                reference,
                reference,
                amountInCents,
                amountCop,
                currency,
                publicKey,
                signature,
                checkoutUrl,
                outletId,
                outlet.getName(),
                subscription.companyId(),
                companyEmail
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
                defaultMonthlyAmount != null ? defaultMonthlyAmount : new BigDecimal("60000.00"),
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
