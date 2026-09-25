package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for FindAllCompaniesUseCaseImpl")
class FindAllCompaniesUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyPort;

    @Mock
    private CurrentUserProviderPort currentUserProvider;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private FindAllCompaniesUseCaseImpl useCase;

    private MasterTree tree;

    @BeforeEach
    void setUp() {
        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot rvwStatus = new MasterRoot(2L, "RVW", "EN REVISION", null, 1L);
        MasterRoot inaStatus = new MasterRoot(4L, "INA", "INACTIVO", null, 1L);

        tree = new MasterTree(List.of(actStatus, rvwStatus, inaStatus));
        lenient().when(masterTreeProvider.getTree()).thenReturn(tree);
    }

    @Test
    @DisplayName("Should return only active companies and filter out pending RVW companies")
    void shouldReturnOnlyActiveCompanies() {
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

        CompanyDomain activeCompany = new CompanyDomain(1L, "900111222-1", "Empresa Activa", "activa@test.com", 1L, BigDecimal.ZERO, LocalDateTime.now(), null);
        CompanyDomain pendingCompany = new CompanyDomain(2L, "900333444-2", "Empresa en Revision", "revision@test.com", 2L, BigDecimal.ZERO, LocalDateTime.now(), null);
        CompanyDomain inactiveCompany = new CompanyDomain(3L, "900555666-3", "Empresa Inactiva", "inactiva@test.com", 4L, BigDecimal.ZERO, LocalDateTime.now(), null);

        when(companyPort.findAll()).thenReturn(List.of(activeCompany, pendingCompany, inactiveCompany));

        List<CompanyResponse> result = useCase.execute();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Empresa Activa", result.get(0).name());
        assertEquals(1L, result.get(0).statusId());
        assertEquals("ACT", result.get(0).status().code());
    }
}
