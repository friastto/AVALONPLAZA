package org.frias.avalon.domain.product.application.usecase.propagate;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropagateCompanyProductToOutletsUseCaseImplTest {

    @Mock
    private JpaProductCompanyRepository productCompanyRepository;

    @Mock
    private JpaGlobalProductRepository globalProductRepository;

    @Mock
    private JpaProductOutletRepository productOutletRepository;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private BarcodeRepositoryPort barcodeRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private PlatformTransactionManager transactionManager;

    private PropagateCompanyProductToOutletsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PropagateCompanyProductToOutletsUseCaseImpl(
                productCompanyRepository,
                globalProductRepository,
                productOutletRepository,
                outletRepositoryPort,
                barcodeRepositoryPort,
                masterTreeProvider,
                transactionManager
        );
    }

    @Test
    @DisplayName("Debe propagar un producto corporativo aprobado a todas las tiendas de la empresa")
    void shouldPropagateProductToAllCompanyOutlets() {
        Long productCompanyId = 10L;
        Long productId = 100L;
        Long companyId = 5L;

        ProductCompanyEntity productCompany = ProductCompanyEntity.builder()
                .id(productCompanyId)
                .productId(productId)
                .companyId(companyId)
                .customPrice(new BigDecimal("3500.00"))
                .customImageUrl("https://s3.avalon.com/corp-img.png")
                .statusId(1L)
                .build();

        Product globalProduct = Product.builder()
                .id(productId)
                .name("Arroz Diana 1kg")
                .description("Arroz blanco seleccionado")
                .barcode("7701234567890")
                .categoryId(12L)
                .unitMeasureId(15L)
                .imageUrl("https://s3.avalon.com/global-img.png")
                .statusId(1L)
                .build();

        OutletDomain outlet1 = mock(OutletDomain.class);
        when(outlet1.getId()).thenReturn(1L);
        when(outlet1.getCompanyId()).thenReturn(companyId);

        OutletDomain outlet2 = mock(OutletDomain.class);
        when(outlet2.getId()).thenReturn(2L);
        when(outlet2.getCompanyId()).thenReturn(companyId);

        when(productCompanyRepository.findById(productCompanyId)).thenReturn(Optional.of(productCompany));
        when(globalProductRepository.findById(productId)).thenReturn(Optional.of(globalProduct));
        when(outletRepositoryPort.findByCompanyId(companyId)).thenReturn(List.of(outlet1, outlet2));

        MasterTree mockTree = mock(MasterTree.class);
        MasterRoot actStatus = mock(MasterRoot.class);
        when(actStatus.getId()).thenReturn(1L);
        when(mockTree.getByCode("ACT")).thenReturn(actStatus);
        when(masterTreeProvider.getTree()).thenReturn(mockTree);

        when(productOutletRepository.findByProductCompanyIdAndOutletId(any(), any())).thenReturn(Optional.empty());
        when(productOutletRepository.save(any(ProductOutlet.class))).thenAnswer(invocation -> {
            ProductOutlet po = invocation.getArgument(0);
            po.setId(99L);
            return po;
        });

        when(barcodeRepositoryPort.findByCode("7701234567890")).thenReturn(Optional.empty());

        int count = useCase.execute(productCompanyId);

        assertEquals(2, count);
        verify(productOutletRepository, org.mockito.Mockito.times(2)).save(any(ProductOutlet.class));
        verify(barcodeRepositoryPort, org.mockito.Mockito.times(2)).save(any(BarcodeDomain.class));
    }

    @Test
    @DisplayName("Debe retornar 0 si la empresa no tiene tiendas registradas")
    void shouldReturnZeroWhenCompanyHasNoOutlets() {
        Long productCompanyId = 10L;
        Long productId = 100L;
        Long companyId = 5L;

        ProductCompanyEntity productCompany = ProductCompanyEntity.builder()
                .id(productCompanyId)
                .productId(productId)
                .companyId(companyId)
                .customPrice(new BigDecimal("3500.00"))
                .build();

        Product globalProduct = Product.builder()
                .id(productId)
                .name("Arroz Diana 1kg")
                .build();

        when(productCompanyRepository.findById(productCompanyId)).thenReturn(Optional.of(productCompany));
        when(globalProductRepository.findById(productId)).thenReturn(Optional.of(globalProduct));
        when(outletRepositoryPort.findByCompanyId(companyId)).thenReturn(List.of());

        int count = useCase.execute(productCompanyId);

        assertEquals(0, count);
        verify(productOutletRepository, never()).save(any());
    }
}
