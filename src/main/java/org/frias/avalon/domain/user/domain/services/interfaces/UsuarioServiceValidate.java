package org.frias.avalon.domain.user.domain.services.interfaces;

import org.frias.avalon.domain.user.domain.dtos.request.UserValidateCredentials;

public interface UsuarioServiceValidate {

    Boolean validateUser(UserValidateCredentials userValidateCredentials);


}
