package org.frias.avalon.domain.person.application.usecase.find;

import org.frias.avalon.domain.person.application.dto.response.PersonResponse;

public interface FindPersonByNumberidUseCase {
    PersonResponse execute(String numberid);
}