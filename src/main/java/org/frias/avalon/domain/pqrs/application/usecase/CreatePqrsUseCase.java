package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.request.CreatePqrsRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;

public interface CreatePqrsUseCase {
    PqrsResponse execute(CreatePqrsRequest request);
}
