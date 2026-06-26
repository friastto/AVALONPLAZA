package org.frias.avalon.domain.person.application.usecase.find;

import org.frias.avalon.domain.person.application.dto.response.PersonDetailResponseDto;

public interface FindPersonByDocumentUseCase {
    PersonDetailResponseDto execute(String numberid);
}
