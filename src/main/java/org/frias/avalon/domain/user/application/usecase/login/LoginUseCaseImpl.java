package org.frias.avalon.domain.user.application.usecase.login;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.jwt.config.CustomUserDetailsService;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.AuthorizationResult;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.application.service.AuthorizationMachine;
import org.frias.avalon.domain.user.application.service.ModesMachine;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginUseCaseImpl implements LoginUseCase{

    //private final RoleAssignmentRepositoryPort roleAssignmentPort;

    private final UserAvalonRepositoryPort userPort;
    private final OutletRepositoryPort outletPort;
    private final MasterTreeProvider masterTreeProvider;
    private final UserAvalonMapper mapper;
    private final  AuthorizationMachine authMachine;
    private final ModesMachine mode;
    private final RoleAssignmentRepositoryPort rolesPort;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public LoginUseCaseImpl(UserAvalonRepositoryPort userPort, OutletRepositoryPort outletPort, MasterTreeProvider masterTreeProvider, UserAvalonMapper mapper, AuthorizationMachine authMachine, ModesMachine mode, RoleAssignmentRepositoryPort rolesPort, JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.userPort = userPort;
        this.outletPort = outletPort;
        this.masterTreeProvider = masterTreeProvider;
        this.mapper = mapper;
        this.authMachine = authMachine;
        this.mode = mode;
        this.rolesPort = rolesPort;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }


    @Override
    public AuthResponse execute(AuthRequest request) {

        UserAvalonDomain user = userPort.findByUserName(request.userName())
                .orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));

        var tree = masterTreeProvider.getTree();

        MasterRoot status = tree.getById(user.getStatusId());

        if (status == null) {
            throw new IllegalStateException("Estado inconsistente en cache");
        }

        if (tree.is(status, "BAN") || tree.is(status, "LOKUSER")) {
            throw new IllegalStateException("Usuario no puede autenticarse");
        }

       if( PassSecure.verifyPassword(request.password(), user.getHashSalt(),user.getHashPassword()))
           throw new IllegalStateException("Credenciales inválidas");

        // --- Lógica de generación de JWT ---
        // 1. Obtener UserDetails completo con roles y estado de cuenta
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.userName());
        // 2. Verificar que el UserDetails obtenido es válido (ej. no está bloqueado, etc.)
        //    La lógica de CustomUserDetailsService ya maneja esto al construir el UserDetails.
        //    Si el usuario no está habilitado, el UserDetails.isEnabled() será false.
        //    Podrías añadir una verificación aquí si quieres lanzar una excepción específica de negocio.
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            throw new IllegalStateException("La cuenta del usuario no está activa o está bloqueada.");
        }
        // --- Fin Lógica de generación de JWT ---


        AuthorizationResult authz = authMachine.resolve(user);

        List<RoleAssignmentDomain> roleAssigned = rolesPort.findByUser(user.getId());

        RoleAssignmentDomain employeeAssignment = roleAssigned.stream()
                .filter(assignment ->
                        assignment.getOutletId() != null
                                && tree.isChildOf(
                                tree.getById(assignment.getRoleId()),
                                "EMP")
                                && tree.is(tree.getById(assignment.getStatus()),"ACT")

                )
                .findFirst()
                .orElse(null);


        // se deve buscar la outlet si el usuario tiene un empleado

        OutletDomain outlet = null;
        if (employeeAssignment != null){
            outlet = outletPort.findById(employeeAssignment.getOutletId())
                    .orElseThrow(() -> new BusinessException("este usuario tiene un perfil de empleado pero no esta asignado a una tienda dentro de avalon"));
        }


        ModesResult modesResult = mode.resolve(roleAssigned,outlet);


        UserAvalonResponseDto userDto = mapper.toResponse(user,status);

        ModesResponseDto modesResponse = mode.mapperToResponse(modesResult);


String token = jwtUtils.generateToken(userDetails, outlet != null ? outlet.getId() : null);

        return new AuthResponse(
                token,
                userDto,
                //authz.roles(),
               // authz.permissions(),
                modesResponse

        );
    }
}
