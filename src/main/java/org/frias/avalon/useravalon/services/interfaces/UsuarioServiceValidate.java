package org.frias.avalon.useravalon.services.interfaces;

import org.frias.avalon.useravalon.dtos.UserValidateCredentials;

public interface UsuarioServiceValidate {

    Boolean validateUser(UserValidateCredentials userValidateCredentials);
}
