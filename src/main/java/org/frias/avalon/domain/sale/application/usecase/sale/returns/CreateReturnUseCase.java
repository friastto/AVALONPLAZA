package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.domain.sale.application.dto.request.CreateReturnRequest;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;

public interface CreateReturnUseCase {
    ReturnResponse execute(CreateReturnRequest request);
}
