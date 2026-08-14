package org.frias.avalon.domain.user.application.service;

import lombok.AllArgsConstructor;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BuildAuthenticationResponseImpl implements BuildAuthenticationResponse {

    private final RoleAssignmentRepositoryPort rolesPort;
    private final UserAvalonOutletResolverService userOutletResolverService;
    private final ModesMachine modeMachine;
    private final TokenOrchestrationService tokenOrchestrationService;
    private final UserAvalonMapper userMapper;
    private final MasterTreeProvider masterTreeProvider;


    @Override
    public AuthResponse buildAuthenticationResponse(UserAvalonDomain avalonDomain, UserDetails userDetails) {
        // 1. Obtener los roles asignados
        List<RoleAssignmentDomain> roleAssigned = rolesPort.findByUserAvalonId(avalonDomain.getId());

        // 2. Resolver el Outlet del empleado
        OutletDomain outlet = userOutletResolverService.resolveActiveOutlet(roleAssigned);

        // 3. Generar los tokens
        TokenRefreshResult tokens = tokenOrchestrationService.generateTokens(
                avalonDomain,
                userDetails,
                roleAssigned
        );

        // 4. Construir los DTOs de usuario y modos
        MasterRoot status = masterTreeProvider.getTree().getById(avalonDomain.getStatusId());
        UserAvalonResponseDto userDto = userMapper.toResponse(avalonDomain, status);
        ModesResult modesResult = modeMachine.resolve(roleAssigned, outlet);
        ModesResponseDto modesResponse = modeMachine.mapperToResponse(modesResult);

        // 5. Ensamblar y devolver la respuesta final
        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                userDto,
                modesResponse
        );
    }
}
