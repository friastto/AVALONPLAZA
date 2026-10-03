package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.model.SubscriptionPaymentDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.frias.avalon.domain.subscription.domain.port.SubscriptionPaymentRepositoryPort;
import org.frias.avalon.domain.subscription.domain.service.BillingCycleCalculatorService;
import org.frias.avalon.domain.subscription.domain.service.WompiSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Idempotent processor of Wompi webhook notifications.
 */
@Service
public class ProcessWompiWebhookUseCaseImpl implements ProcessWompiWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessWompiWebhookUseCaseImpl.class);

    private final SubscriptionPaymentRepositoryPort paymentPort;
    private final OutletSubscriptionRepositoryPort subscriptionPort;
    private final BillingCycleCalculatorService billingCycleCalculatorService;
    private final WompiSecurityService wompiSecurityService;
    private final MasterTreeProvider masterTreeProvider;

    public ProcessWompiWebhookUseCaseImpl(
            SubscriptionPaymentRepositoryPort paymentPort,
            OutletSubscriptionRepositoryPort subscriptionPort,
            BillingCycleCalculatorService billingCycleCalculatorService,
            WompiSecurityService wompiSecurityService,
            MasterTreeProvider masterTreeProvider
    ) {
        this.paymentPort = paymentPort;
        this.subscriptionPort = subscriptionPort;
        this.billingCycleCalculatorService = billingCycleCalculatorService;
        this.wompiSecurityService = wompiSecurityService;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void execute(Map<String, Object> payload, String rawBody) {
        if (payload == null) {
            log.warn("Wompi webhook received with null payload");
            return;
        }

        String event = (String) payload.get("event");
        if (!"transaction.updated".equalsIgnoreCase(event)) {
            log.info("Ignoring non-transaction Wompi event: {}", event);
            return;
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            log.warn("Wompi webhook missing 'data' object");
            return;
        }

        Map<String, Object> transaction = (Map<String, Object>) data.get("transaction");
        if (transaction == null) {
            log.warn("Wompi webhook missing 'transaction' object");
            return;
        }

        String transactionId = (String) transaction.get("id");
        String reference = (String) transaction.get("reference");
        String status = (String) transaction.get("status");
        String paymentMethodType = (String) transaction.get("payment_method_type");

        log.info("Processing Wompi webhook for ref: {}, tx: {}, status: {}", reference, transactionId, status);

        if (reference == null || reference.isBlank()) {
            log.error("Wompi transaction missing reference");
            return;
        }

        // Optional signature verification if signature block is provided by Wompi
        Map<String, Object> signatureMap = (Map<String, Object>) payload.get("signature");
        if (signatureMap != null) {
            String checksum = (String) signatureMap.get("checksum");
            List<String> properties = (List<String>) signatureMap.get("properties");
            Object timestampObj = payload.get("timestamp");
            String timestamp = timestampObj != null ? String.valueOf(timestampObj) : "";

            if (properties != null && checksum != null) {
                StringBuilder concatValues = new StringBuilder();
                for (String prop : properties) {
                    Object val = resolveNestedProperty(payload, prop);
                    if (val != null) {
                        concatValues.append(val);
                    }
                }
                boolean valid = wompiSecurityService.validateWebhookSignature(concatValues.toString(), timestamp, checksum);
                if (!valid) {
                    log.warn("Wompi webhook signature validation failed for reference: {}", reference);
                }
            }
        }

        SubscriptionPaymentDomain payment = paymentPort.findByWompiReference(reference)
                .orElse(null);

        if (payment == null) {
            log.warn("No pending payment found for reference: {}", reference);
            return;
        }

        // Idempotency check: if already approved, do not double-extend
        if ("APPROVED".equalsIgnoreCase(payment.status())) {
            log.info("Payment for reference {} was already processed as APPROVED", reference);
            return;
        }

        // Update payment record
        SubscriptionPaymentDomain updatedPayment = new SubscriptionPaymentDomain(
                payment.id(),
                payment.outletSubscriptionId(),
                payment.outletId(),
                payment.companyId(),
                transactionId,
                payment.wompiReference(),
                paymentMethodType,
                payment.amountInCents(),
                payment.currency(),
                status != null ? status.toUpperCase() : "UNKNOWN",
                payment.checksumSent(),
                rawBody,
                payment.createdAt(),
                LocalDateTime.now()
        );
        paymentPort.save(updatedPayment);

        if (!"APPROVED".equalsIgnoreCase(status)) {
            log.info("Wompi transaction not approved (status: {}). No subscription extension performed.", status);
            return;
        }

        // Extend outlet subscription
        OutletSubscriptionDomain subscription = subscriptionPort.findById(payment.outletSubscriptionId())
                .orElse(null);

        if (subscription == null) {
            log.error("Outlet subscription not found for id: {}", payment.outletSubscriptionId());
            return;
        }

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot subAct = tree.getByCode("SUB_ACT");
        if (subAct == null) {
            throw new IllegalStateException("MasterData node SUB_ACT not found in MasterTree");
        }

        LocalDateTime baseDate = subscription.currentPeriodEnd().isAfter(LocalDateTime.now())
                ? subscription.currentPeriodEnd()
                : LocalDateTime.now();

        int billingDay = subscription.billingDay() != null ? subscription.billingDay() : BillingCycleCalculatorService.DEFAULT_BILLING_DAY;
        LocalDateTime nextPeriodEnd = billingCycleCalculatorService.calculateNextPeriodEnd(baseDate, billingDay);
        LocalDateTime nextGraceEnd = nextPeriodEnd.plusDays(BillingCycleCalculatorService.GRACE_DAYS);

        OutletSubscriptionDomain renewedSubscription = new OutletSubscriptionDomain(
                subscription.id(),
                subscription.outletId(),
                subscription.companyId(),
                subAct.getId(),
                billingDay,
                subscription.amountCop(),
                subscription.trialEndsAt(),
                baseDate,
                nextPeriodEnd,
                nextGraceEnd,
                subscription.createdAt(),
                LocalDateTime.now()
        );
        subscriptionPort.save(renewedSubscription);

        log.info("Successfully renewed subscription for outlet {} until {}", subscription.outletId(), nextPeriodEnd);
    }

    @SuppressWarnings("unchecked")
    private Object resolveNestedProperty(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map<?, ?> m) {
                current = m.get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
