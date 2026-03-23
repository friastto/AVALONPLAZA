package org.frias.avalon.domain.usergeneral.useravalon.services.interfaces;

import org.frias.avalon.temp.features.auth.dtos.UserValidateCredentials;

public interface UsuarioServiceValidate {

    Boolean validateUser(UserValidateCredentials userValidateCredentials);


}
