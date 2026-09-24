package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FindNearbyProductSuggestionsUseCaseImpl implements FindNearbyProductSuggestionsUseCase {

    private final OutletRepositoryPort outletRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public FindNearbyProductSuggestionsUseCaseImpl(
            OutletRepositoryPort outletRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort,
            PlatformTransactionManager transactionManager
    ) {
        this.outletRepositoryPort = outletRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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

        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        Set<String> distinctSuggestions = new LinkedHashSet<>();

        try {
            for (OutletLocationInfo outlet : nearbyOutlets) {
                if (outlet.id() == null) continue;
                TenantContext.setTenantOutletId(outlet.id());

                try {
                    transactionTemplate.execute(status -> {
                        Page<ProductDomain> products = productOutletRepositoryPort.findAvailableByName(
                                sanitizedQuery,
                                outlet.id(),
                                PageRequest.of(0, 10)
                        );
                        if (products != null && products.hasContent()) {
                            for (ProductDomain p : products.getContent()) {
                                if (p.getName() != null && !p.getName().isBlank()) {
                                    distinctSuggestions.add(p.getName().trim());
                                }
                                if (distinctSuggestions.size() >= 8) {
                                    break;
                                }
                            }
                        }
                        return null;
                    });
                } catch (Exception ignored) {
                    // Continuar con las demas tiendas si una falla
                }

                if (distinctSuggestions.size() >= 8) {
                    break;
                }
            }
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }

        return new ArrayList<>(distinctSuggestions);
    }
}
