package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.request.UpdatePqrsStatusRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;

public interface UpdatePqrsStatusUseCase {
    PqrsResponse execute(Long id, UpdatePqrsStatusRequest request);
}
