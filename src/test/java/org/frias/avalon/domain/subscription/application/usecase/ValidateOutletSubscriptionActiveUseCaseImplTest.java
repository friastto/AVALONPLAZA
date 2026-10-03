package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.core.exeptions.SubscriptionSuspendedException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;
import org.frias.avalon.domain.subscription.domain.port.OutletSubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ValidateOutletSubscriptionActiveUseCaseImpl")
class ValidateOutletSubscriptionActiveUseCaseImplTest {

    @Mock
    private OutletSubscriptionRepositoryPort subscriptionPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private MasterTree masterTree;

    private ValidateOutletSubscriptionActiveUseCaseImpl useCase;

    private static final Long OUTLET_ID = 10L;
    private static final Long STATUS_ACT_ID = 701L;
    private static final Long STATUS_SUSP_ID = 703L;

    @BeforeEach
    void setUp() {
        useCase = new ValidateOutletSubscriptionActiveUseCaseImpl(subscriptionPort, masterTreeProvider);
    }

    @Test
    @DisplayName("Should pass without exception when outlet has no subscription record yet")
    void shouldPassWhenSubscriptionNotFound() {
        when(subscriptionPort.findByOutletId(OUTLET_ID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> useCase.execute(OUTLET_ID));
    }

    @Test
    @DisplayName("Should pass when subscription is active and within grace period")
    void shouldPassWhenActiveAndWithinPeriod() {
        OutletSubscriptionDomain sub = new OutletSubscriptionDomain(
                1L, OUTLET_ID, 1L, STATUS_ACT_ID,
                28, new BigDecimal("60000.00"),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(15),
                LocalDateTime.now().plusDays(22),
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(10)
        );

        when(subscriptionPort.findByOutletId(OUTLET_ID)).thenReturn(Optional.of(sub));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot actNode = new MasterRoot(STATUS_ACT_ID, "SUB_ACT", "Activa", null, 1L);
        when(masterTree.getById(STATUS_ACT_ID)).thenReturn(actNode);
        when(masterTree.is(actNode, "SUB_SUSP")).thenReturn(false);
        when(masterTree.is(actNode, "SUB_CANC")).thenReturn(false);

        assertDoesNotThrow(() -> useCase.execute(OUTLET_ID));
    }

    @Test
    @DisplayName("Should throw SubscriptionSuspendedException when grace period has expired")
    void shouldThrowWhenGracePeriodExpired() {
        OutletSubscriptionDomain sub = new OutletSubscriptionDomain(
                1L, OUTLET_ID, 1L, STATUS_ACT_ID,
                28, new BigDecimal("60000.00"),
                LocalDateTime.now().minusDays(40),
                LocalDateTime.now().minusDays(40),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(3), // Expired 3 days ago
                LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(40)
        );

        when(subscriptionPort.findByOutletId(OUTLET_ID)).thenReturn(Optional.of(sub));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot actNode = new MasterRoot(STATUS_ACT_ID, "SUB_ACT", "Activa", null, 1L);
        when(masterTree.getById(STATUS_ACT_ID)).thenReturn(actNode);

        assertThrows(SubscriptionSuspendedException.class, () -> useCase.execute(OUTLET_ID));
    }

    @Test
    @DisplayName("Should throw SubscriptionSuspendedException when status is SUB_SUSP")
    void shouldThrowWhenStatusIsSuspended() {
        OutletSubscriptionDomain sub = new OutletSubscriptionDomain(
                1L, OUTLET_ID, 1L, STATUS_SUSP_ID,
                28, new BigDecimal("60000.00"),
                LocalDateTime.now().minusDays(40),
                LocalDateTime.now().minusDays(40),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(17),
                LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(40)
        );

        when(subscriptionPort.findByOutletId(OUTLET_ID)).thenReturn(Optional.of(sub));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot suspNode = new MasterRoot(STATUS_SUSP_ID, "SUB_SUSP", "Suspendida", null, 1L);
        when(masterTree.getById(STATUS_SUSP_ID)).thenReturn(suspNode);
        when(masterTree.is(suspNode, "SUB_SUSP")).thenReturn(true);

        assertThrows(SubscriptionSuspendedException.class, () -> useCase.execute(OUTLET_ID));
    }
}
