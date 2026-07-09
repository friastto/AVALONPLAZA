package org.frias.avalon.domain.credit.application.usecase.payment;

import org.frias.avalon.domain.credit.application.dto.request.RegisterPaymentRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;

public interface RegisterPaymentUseCase {
    CreditTransactionResponse execute(RegisterPaymentRequest request);
}
