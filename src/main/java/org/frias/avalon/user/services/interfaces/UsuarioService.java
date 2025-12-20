package org.frias.avalon.user.services.interfaces;

import org.frias.avalon.user.dtos.UserResponseDto;
import org.frias.avalon.user.entities.User;

public interface UsuarioService {

    User searchByUserName(String userName);
    User getUserEmployeeStatus(String numberId);
}
