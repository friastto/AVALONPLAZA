package org.frias.avalon.domain.usergeneral.useravalon.services.interfaces;

import org.frias.avalon.domain.usergeneral.auth.dtos.request.UserValidateCredentials;

public interface UsuarioServiceValidate {

    Boolean validateUser(UserValidateCredentials userValidateCredentials);


}
