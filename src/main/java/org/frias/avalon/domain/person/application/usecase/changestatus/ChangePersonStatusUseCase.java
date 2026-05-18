package org.frias.avalon.domain.person.application.usecase.changestatus;

import org.frias.avalon.domain.person.application.dto.response.PersonResponse;

public interface ChangePersonStatusUseCase {
    PersonResponse execute(Long idPerson, Long idStatus);
}
