package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.springframework.security.core.userdetails.UserDetails;

public interface BuildAuthenticationResponse {
    AuthResponse buildAuthenticationResponse(UserAvalonDomain user, UserDetails userDetails);

}
