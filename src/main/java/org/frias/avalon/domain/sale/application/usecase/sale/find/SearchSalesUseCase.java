package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;

import java.util.List;

public interface SearchSalesUseCase {
    List<SaleResponse> search(Long outletId, String query);
    List<SaleResponse> getRecentSales(Long outletId);
    SaleResponse findByFlexibleCode(String codeOrSearch, Long outletId);
}
