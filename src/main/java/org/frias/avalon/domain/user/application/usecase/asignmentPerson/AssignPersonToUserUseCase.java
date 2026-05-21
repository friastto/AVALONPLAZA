package org.frias.avalon.domain.user.application.usecase.asignmentPerson;

import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonDto;


public interface AssignPersonToUserUseCase {

    UserAvalonDto execute(Long userId, CreatePersonRequest data);
}