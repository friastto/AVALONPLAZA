package org.frias.avalon.domain.credit.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.credit.application.dto.request.CreateCreditAccountRequest;
import org.frias.avalon.domain.credit.application.dto.request.UpdateCreditLimitRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.application.usecase.create.CreateCreditAccountUseCaseImpl;
import org.frias.avalon.domain.credit.application.usecase.find.FindCreditAccountByClientUseCaseImpl;
import org.frias.avalon.domain.credit.application.usecase.limit.UpdateCreditLimitUseCaseImpl;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for Credit Module Use Cases")
class CreditUseCaseTest {

    @Mock
    private CreditRepositoryPort creditRepositoryPort;

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private JpaOutletRepository jpaOutletRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private CreateCreditAccountUseCaseImpl createCreditAccountUseCase;
    private FindCreditAccountByClientUseCaseImpl findCreditAccountUseCase;
    private UpdateCreditLimitUseCaseImpl updateCreditLimitUseCase;

    private PersonDomain mockClient;
    private MasterTree mockTree;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        Outlet mockOutlet = new Outlet();
        mockOutlet.setId(1L);
        mockOutlet.setCompanyId(10L);
        lenient().when(jpaOutletRepository.findById(1L)).thenReturn(Optional.of(mockOutlet));

        createCreditAccountUseCase = new CreateCreditAccountUseCaseImpl(
                creditRepositoryPort,
                personRepositoryPort,
                masterTreeProvider,
                jpaOutletRepository,
                transactionManager
        );

        findCreditAccountUseCase = new FindCreditAccountByClientUseCaseImpl(
                creditRepositoryPort,
                personRepositoryPort,
                masterTreeProvider
        );

        updateCreditLimitUseCase = new UpdateCreditLimitUseCaseImpl(
                creditRepositoryPort,
                personRepositoryPort,
                masterTreeProvider
        );

        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        mockTree = new MasterTree(List.of(actStatus));
        lenient().when(masterTreeProvider.getTree()).thenReturn(mockTree);

        mockClient = PersonDomain.createFromEntity(
                10L,
                "12345678",
                "Juan",
                "Perez",
                "Calle 10",
                1L,
                1L,
                3001234567L,
                "juan@test.com",
                1L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create credit account successfully with dynamic ACT status")
    void shouldCreateCreditAccountSuccessfully() {
        CreateCreditAccountRequest request = new CreateCreditAccountRequest(
                "12345678",
                1L,
                new BigDecimal("500000")
        );

        when(personRepositoryPort.findByNumberid("12345678")).thenReturn(Optional.of(mockClient));
        when(creditRepositoryPort.findByClientIdAndOutletId(10L, 1L)).thenReturn(Optional.empty());

        CreditAccountDomain savedAccount = CreditAccountDomain.reconstruct(
                100L,
                10L,
                1L,
                new BigDecimal("500000"),
                BigDecimal.ZERO,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenReturn(savedAccount);

        CreditAccountResponse response = createCreditAccountUseCase.execute(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("ACTIVO", response.status());
        assertEquals(new BigDecimal("500000"), response.creditLimit());
        verify(creditRepositoryPort).save(any(CreditAccountDomain.class));
    }

    @Test
    @DisplayName("Should throw BusinessException if credit account already exists")
    void shouldThrowExceptionWhenAccountAlreadyExists() {
        CreateCreditAccountRequest request = new CreateCreditAccountRequest(
                "12345678",
                1L,
                new BigDecimal("500000")
        );

        when(personRepositoryPort.findByNumberid("12345678")).thenReturn(Optional.of(mockClient));
        CreditAccountDomain existing = CreditAccountDomain.create(10L, 1L, new BigDecimal("500000"), 1L);
        when(creditRepositoryPort.findByClientIdAndOutletId(10L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> createCreditAccountUseCase.execute(request));
        verify(creditRepositoryPort, never()).save(any(CreditAccountDomain.class));
    }

    @Test
    @DisplayName("Should auto-initialize credit account when findOrCreate does not find one")
    void shouldAutoInitializeCreditAccountInFindOrCreate() {
        when(personRepositoryPort.findByNumberid("12345678")).thenReturn(Optional.of(mockClient));
        when(creditRepositoryPort.findByClientIdAndOutletId(10L, 1L)).thenReturn(Optional.empty());

        CreditAccountDomain savedAccount = CreditAccountDomain.reconstruct(
                200L,
                10L,
                1L,
                new BigDecimal("150000"),
                BigDecimal.ZERO,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenReturn(savedAccount);

        CreditAccountResponse response = findCreditAccountUseCase.findOrCreate("12345678", 1L);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals(new BigDecimal("150000"), response.creditLimit());
        assertEquals("ACTIVO", response.status());
        verify(creditRepositoryPort).save(any(CreditAccountDomain.class));
    }

    @Test
    @DisplayName("Should update credit limit successfully")
    void shouldUpdateCreditLimitSuccessfully() {
        UpdateCreditLimitRequest request = new UpdateCreditLimitRequest(100L, new BigDecimal("800000"));

        CreditAccountDomain existing = CreditAccountDomain.reconstruct(
                100L,
                10L,
                1L,
                new BigDecimal("500000"),
                BigDecimal.ZERO,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(creditRepositoryPort.findById(100L)).thenReturn(Optional.of(existing));
        when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenReturn(existing);
        when(personRepositoryPort.findById(10L)).thenReturn(Optional.of(mockClient));

        CreditAccountResponse response = updateCreditLimitUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("800000"), response.creditLimit());
        assertEquals("ACTIVO", response.status());
        verify(creditRepositoryPort).save(existing);
    }
}
