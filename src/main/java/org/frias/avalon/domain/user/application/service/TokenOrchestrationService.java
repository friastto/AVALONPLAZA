package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;


public interface TokenOrchestrationService {
    TokenRefreshResult generateTokens(UserAvalonDomain user, UserDetails userDetails, List<RoleAssignmentDomain> roleAssigned);
}