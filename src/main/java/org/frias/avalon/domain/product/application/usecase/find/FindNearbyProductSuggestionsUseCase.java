package org.frias.avalon.domain.product.application.usecase.find;

import java.util.List;

public interface FindNearbyProductSuggestionsUseCase {
    List<String> execute(Double latitude, Double longitude, int radius, String query);
}
