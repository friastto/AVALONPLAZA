package org.frias.avalon.domain.person.application.usecase.create;

import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;

public interface CreatePersonUseCase {
    PersonResponse execute(CreatePersonRequest request);
}