package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.user.application.dtos.request.AuthorizationResult;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;

public interface AuthorizationMachine {
    AuthorizationResult resolve(UserAvalonDomain user);
}
