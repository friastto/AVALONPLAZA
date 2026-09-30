package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FindNearbyProductSuggestionsUseCaseImpl implements FindNearbyProductSuggestionsUseCase {

    private final OutletRepositoryPort outletRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;

    public FindNearbyProductSuggestionsUseCaseImpl(
            OutletRepositoryPort outletRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort
    ) {
        this.outletRepositoryPort = outletRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
    }

    @Override
    public List<String> execute(Double latitude, Double longitude, int radius, String query) {
        if (latitude == null || longitude == null || query == null || query.trim().length() < 2) {
            return List.of();
        }

        String sanitizedQuery = query.trim();
        List<OutletLocationInfo> nearbyOutlets = outletRepositoryPort.findNearbyByRadiusLight(latitude, longitude, radius, null);
        if (nearbyOutlets == null || nearbyOutlets.isEmpty()) {
            return List.of();
        }

        // Limitar a las tiendas mas cercanas candidatas (Top 12)
        int maxStores = Math.min(nearbyOutlets.size(), 12);
        List<OutletLocationInfo> candidateOutlets = nearbyOutlets.subList(0, maxStores);
        List<Long> candidateOutletIds = candidateOutlets.stream().map(OutletLocationInfo::id).toList();

        Map<Long, List<ProductDomain>> productsByOutlet = productOutletRepositoryPort.findAvailableByNameAcrossOutlets(
                sanitizedQuery,
                candidateOutletIds,
                8
        );

        Set<String> distinctSuggestions = new LinkedHashSet<>();
        for (OutletLocationInfo outlet : candidateOutlets) {
            List<ProductDomain> products = productsByOutlet.get(outlet.id());
            if (products != null) {
                for (ProductDomain p : products) {
                    if (p.getName() != null && !p.getName().isBlank()) {
                        distinctSuggestions.add(p.getName().trim());
                    }
                    if (distinctSuggestions.size() >= 8) {
                        break;
                    }
                }
            }
            if (distinctSuggestions.size() >= 8) {
                break;
            }
        }

        return new ArrayList<>(distinctSuggestions);
    }
}
