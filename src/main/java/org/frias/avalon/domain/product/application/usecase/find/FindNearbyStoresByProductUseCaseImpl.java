package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.request.NearbyStoresByProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.NearbyStoreProductResponseDto;
import org.frias.avalon.domain.product.application.dto.response.ProductStockDto;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FindNearbyStoresByProductUseCaseImpl implements FindNearbyStoresByProductUseCase {

    private final OutletRepositoryPort outletRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final UnitConversionService unitConversionService;
    private final MasterTreeProvider masterTreeProvider;
    private final TransactionTemplate transactionTemplate;

    public FindNearbyStoresByProductUseCaseImpl(
            OutletRepositoryPort outletRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort,
            UnitConversionService unitConversionService,
            MasterTreeProvider masterTreeProvider,
            PlatformTransactionManager transactionManager
    ) {
        this.outletRepositoryPort = outletRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.unitConversionService = unitConversionService;
        this.masterTreeProvider = masterTreeProvider;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public List<NearbyStoreProductResponseDto> execute(NearbyStoresByProductRequestDto request) {
        if (request == null || request.location() == null || request.query() == null || request.query().trim().isEmpty()) {
            return List.of();
        }

        String sanitizedQuery = request.query().trim();
        List<OutletLocationInfo> nearbyOutlets = outletRepositoryPort.findNearbyByRadiusLight(
                request.location().lat(),
                request.location().lon(),
                request.radius() > 0 ? request.radius() : 2000,
                null
        );

        if (nearbyOutlets == null || nearbyOutlets.isEmpty()) {
            return List.of();
        }

        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        List<NearbyStoreProductResponseDto> resultStores = new ArrayList<>();

        try {
            for (OutletLocationInfo outlet : nearbyOutlets) {
                if (outlet.id() == null) continue;
                TenantContext.setTenantOutletId(outlet.id());

                try {
                    List<ProductStockDto> matchingProducts = transactionTemplate.execute(status -> {
                        Page<ProductDomain> products = productOutletRepositoryPort.findAvailableByName(
                                sanitizedQuery,
                                outlet.id(),
                                PageRequest.of(0, 15)
                        );
                        if (products != null && products.hasContent()) {
                            List<ProductStockDto> items = new ArrayList<>();
                            for (ProductDomain p : products.getContent()) {
                                String displayStock;
                                String unitMeasure = null;
                                if (p.getUnitMeasureId() != null) {
                                    try {
                                        displayStock = unitConversionService.convertFromSmallestUnit(p.getStock(), p.getUnitMeasureId());
                                        MasterRoot unitNode = masterTreeProvider.getTree().getById(p.getUnitMeasureId());
                                        if (unitNode != null) {
                                            unitMeasure = unitNode.getShortName();
                                        }
                                    } catch (Exception e) {
                                        displayStock = p.getStock() != null ? p.getStock().toString() : "0";
                                    }
                                } else {
                                    displayStock = p.getStock() != null ? p.getStock().toString() : "0";
                                }
                                items.add(new ProductStockDto(p.getName(), p.getStock(), displayStock, unitMeasure));
                            }
                            return items;
                        }
                        return List.of();
                    });

                    if (matchingProducts != null && !matchingProducts.isEmpty()) {
                        resultStores.add(new NearbyStoreProductResponseDto(
                                outlet.id(),
                                outlet.name(),
                                outlet.latitude(),
                                outlet.longitude(),
                                matchingProducts
                        ));
                    }
                } catch (Exception ignored) {
                    // Si una tienda falla puntualmente, se continua con las demas
                }
            }
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }

        return resultStores;
    }
}
