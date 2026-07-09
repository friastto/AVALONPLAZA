package org.frias.avalon.domain.credit.application.usecase.limit;

import org.frias.avalon.domain.credit.application.dto.request.UpdateCreditLimitRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;

public interface UpdateCreditLimitUseCase {
    CreditAccountResponse execute(UpdateCreditLimitRequest request);
}
