package org.frias.avalon.domain.product.application.usecase.adopt;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.AdoptGlobalProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.ProductSuggestionResponseDto;
import org.frias.avalon.domain.product.application.usecase.propagate.PropagateCompanyProductToOutletsUseCase;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptGlobalProductUseCaseImplTest {

    @Mock
    private JpaGlobalProductRepository globalProductRepository;

    @Mock
    private JpaProductCompanyRepository productCompanyRepository;

    @Mock
    private PropagateCompanyProductToOutletsUseCase propagateCompanyProductToOutletsUseCase;

    @Mock
    private CompanyRepositoryPort companyRepositoryPort;

    @Mock
    private CurrentUserProviderPort currentUserProvider;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    private AdoptGlobalProductUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AdoptGlobalProductUseCaseImpl(
                globalProductRepository,
                productCompanyRepository,
                propagateCompanyProductToOutletsUseCase,
                companyRepositoryPort,
                currentUserProvider,
                masterTreeProvider
        );
    }

    @Test
    @DisplayName("Debe adoptar un producto global de Nivel 1 y propagarlo en cascada")
    void shouldAdoptGlobalProductAndPropagate() {
        Long productId = 50L;
        Long companyId = 2L;
        BigDecimal price = new BigDecimal("4200.00");

        AdoptGlobalProductRequestDto request = new AdoptGlobalProductRequestDto(
                productId,
                companyId,
                price,
                null
        );

        Product globalProduct = Product.builder()
                .id(productId)
                .name("Aceite Vegetal 1L")
                .description("Aceite refinado")
                .barcode("770999888777")
                .categoryId(10L)
                .unitMeasureId(20L)
                .build();

        when(globalProductRepository.findById(productId)).thenReturn(Optional.of(globalProduct));
        when(currentUserProvider.hasRole("ROLE_GERGEN")).thenReturn(false);

        MasterTree mockTree = mock(MasterTree.class);
        MasterRoot actStatus = mock(MasterRoot.class);
        when(actStatus.getId()).thenReturn(1L);
        when(mockTree.getByCode("ACT")).thenReturn(actStatus);
        when(masterTreeProvider.getTree()).thenReturn(mockTree);

        when(productCompanyRepository.findByProductIdAndCompanyId(productId, companyId)).thenReturn(Optional.empty());
        when(productCompanyRepository.save(any(ProductCompanyEntity.class))).thenAnswer(inv -> {
            ProductCompanyEntity entity = inv.getArgument(0);
            entity.setId(101L);
            return entity;
        });

        CompanyDomain company = mock(CompanyDomain.class);
        when(company.name()).thenReturn("Supermercados Avalon SAS");
        when(companyRepositoryPort.findById(companyId)).thenReturn(Optional.of(company));

        ProductSuggestionResponseDto result = useCase.execute(request);

        assertNotNull(result);
        assertEquals(101L, result.id());
        assertEquals("Aceite Vegetal 1L", result.productName());
        assertEquals(price, result.suggestedPrice());
        assertEquals(companyId, result.companyId());
        assertEquals("Supermercados Avalon SAS", result.companyName());

        verify(propagateCompanyProductToOutletsUseCase).execute(101L);
    }
}
