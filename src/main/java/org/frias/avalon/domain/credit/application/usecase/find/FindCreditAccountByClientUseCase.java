package org.frias.avalon.domain.credit.application.usecase.find;

import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;
import java.util.List;

public interface FindCreditAccountByClientUseCase {
    CreditAccountResponse findOrCreate(String clientNumberid, Long outletId);
    List<CreditTransactionResponse> findTransactions(String clientNumberid, Long outletId);
    List<CreditAccountResponse> findAllByStore(Long outletId);
}
