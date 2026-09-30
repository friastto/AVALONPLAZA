package org.frias.avalon.domain.product.application.usecase.find;

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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FindNearbyStoresByProductUseCaseImpl implements FindNearbyStoresByProductUseCase {

    private final OutletRepositoryPort outletRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final UnitConversionService unitConversionService;
    private final MasterTreeProvider masterTreeProvider;

    public FindNearbyStoresByProductUseCaseImpl(
            OutletRepositoryPort outletRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort,
            UnitConversionService unitConversionService,
            MasterTreeProvider masterTreeProvider
    ) {
        this.outletRepositoryPort = outletRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.unitConversionService = unitConversionService;
        this.masterTreeProvider = masterTreeProvider;
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

        // Limitar a las tiendas mas cercanas candidatas (Top 12 por distancia)
        int maxStores = Math.min(nearbyOutlets.size(), 12);
        List<OutletLocationInfo> candidateOutlets = nearbyOutlets.subList(0, maxStores);
        List<Long> candidateOutletIds = candidateOutlets.stream().map(OutletLocationInfo::id).toList();

        Map<Long, List<ProductDomain>> productsByOutlet = productOutletRepositoryPort.findAvailableByNameAcrossOutlets(
                sanitizedQuery,
                candidateOutletIds,
                15
        );

        List<NearbyStoreProductResponseDto> resultStores = new ArrayList<>();

        for (OutletLocationInfo outlet : candidateOutlets) {
            List<ProductDomain> products = productsByOutlet.get(outlet.id());
            if (products == null || products.isEmpty()) {
                continue;
            }

            List<ProductStockDto> items = new ArrayList<>();
            for (ProductDomain p : products) {
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

            resultStores.add(new NearbyStoreProductResponseDto(
                    outlet.id(),
                    outlet.name(),
                    outlet.latitude(),
                    outlet.longitude(),
                    items
            ));
        }

        return resultStores;
    }
}
