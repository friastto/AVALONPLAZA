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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ProcessWompiWebhookUseCaseImpl")
class ProcessWompiWebhookUseCaseImplTest {

    @Mock
    private SubscriptionPaymentRepositoryPort paymentPort;

    @Mock
    private OutletSubscriptionRepositoryPort subscriptionPort;

    @Mock
    private BillingCycleCalculatorService billingCycleCalculatorService;

    @Mock
    private WompiSecurityService wompiSecurityService;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private MasterTree masterTree;

    private ProcessWompiWebhookUseCaseImpl useCase;

    private static final String REFERENCE = "OUTLET-SUB-4-1711670400000";
    private static final String TX_ID = "wompi-tx-999";
    private static final Long OUTLET_ID = 4L;
    private static final Long COMPANY_ID = 2L;
    private static final Long SUB_ID = 100L;
    private static final Long SUB_ACT_ID = 701L;

    @BeforeEach
    void setUp() {
        useCase = new ProcessWompiWebhookUseCaseImpl(
                paymentPort,
                subscriptionPort,
                billingCycleCalculatorService,
                wompiSecurityService,
                masterTreeProvider
        );
    }

    @Test
    @DisplayName("Should successfully process APPROVED transaction and extend subscription")
    void shouldProcessApprovedTransactionAndExtendSubscription() {
        // Given
        Map<String, Object> payload = Map.of(
                "event", "transaction.updated",
                "data", Map.of(
                        "transaction", Map.of(
                                "id", TX_ID,
                                "reference", REFERENCE,
                                "status", "APPROVED",
                                "payment_method_type", "NEQUI"
                        )
                )
        );

        SubscriptionPaymentDomain pendingPayment = new SubscriptionPaymentDomain(
                50L, SUB_ID, OUTLET_ID, COMPANY_ID,
                null, REFERENCE, "NEQUI",
                15000000L, "COP", "PENDING",
                "checksum", null,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)
        );

        OutletSubscriptionDomain currentSub = new OutletSubscriptionDomain(
                SUB_ID, OUTLET_ID, COMPANY_ID, 700L, // trial
                28, new BigDecimal("60000.00"),
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().plusDays(10), // ends in 10 days
                LocalDateTime.now().plusDays(17),
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(20)
        );

        when(paymentPort.findByWompiReference(REFERENCE)).thenReturn(Optional.of(pendingPayment));
        when(subscriptionPort.findById(SUB_ID)).thenReturn(Optional.of(currentSub));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot actRoot = new MasterRoot(SUB_ACT_ID, "SUB_ACT", "Activa", null, 1L);
        when(masterTree.getByCode("SUB_ACT")).thenReturn(actRoot);

        LocalDateTime calculatedNextEnd = LocalDateTime.of(2026, 5, 28, 23, 59, 59);
        when(billingCycleCalculatorService.calculateNextPeriodEnd(any(), eq(28))).thenReturn(calculatedNextEnd);

        // When
        useCase.execute(payload, "{\"raw\":\"json\"}");

        // Then
        ArgumentCaptor<SubscriptionPaymentDomain> paymentCaptor = ArgumentCaptor.forClass(SubscriptionPaymentDomain.class);
        verify(paymentPort).save(paymentCaptor.capture());
        assertEquals("APPROVED", paymentCaptor.getValue().status());
        assertEquals(TX_ID, paymentCaptor.getValue().wompiTransactionId());

        ArgumentCaptor<OutletSubscriptionDomain> subCaptor = ArgumentCaptor.forClass(OutletSubscriptionDomain.class);
        verify(subscriptionPort).save(subCaptor.capture());
        assertEquals(SUB_ACT_ID, subCaptor.getValue().statusId());
        assertEquals(calculatedNextEnd, subCaptor.getValue().currentPeriodEnd());
    }

    @Test
    @DisplayName("Should be idempotent and ignore already APPROVED payments")
    void shouldIgnoreAlreadyApprovedPayment() {
        // Given
        Map<String, Object> payload = Map.of(
                "event", "transaction.updated",
                "data", Map.of(
                        "transaction", Map.of(
                                "id", TX_ID,
                                "reference", REFERENCE,
                                "status", "APPROVED"
                        )
                )
        );

        SubscriptionPaymentDomain alreadyApproved = new SubscriptionPaymentDomain(
                50L, SUB_ID, OUTLET_ID, COMPANY_ID,
                TX_ID, REFERENCE, "CARD",
                15000000L, "COP", "APPROVED",
                "checksum", "raw",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)
        );

        when(paymentPort.findByWompiReference(REFERENCE)).thenReturn(Optional.of(alreadyApproved));

        // When
        useCase.execute(payload, "raw");

        // Then: No payment save, no subscription save
        verify(paymentPort, never()).save(any());
        verify(subscriptionPort, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore events other than transaction.updated")
    void shouldIgnoreOtherEvents() {
        Map<String, Object> payload = Map.of("event", "some.other.event");

        useCase.execute(payload, "raw");

        verify(paymentPort, never()).findByWompiReference(any());
    }
}
