package org.frias.avalon.domain.credit.application.usecase.create;

import org.frias.avalon.domain.credit.application.dto.request.CreateCreditAccountRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;

public interface CreateCreditAccountUseCase {
    CreditAccountResponse execute(CreateCreditAccountRequest request);
}
