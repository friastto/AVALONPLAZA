package org.frias.avalon.domain.user.application.usecase.impersonate;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.jwt.config.CustomUserDetailsService;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.AdminAvalonModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ClientModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.EmployeeModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.service.PermissionService;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Deprecated
@Service
@AllArgsConstructor
public class ImpersonateOutletUseCaseImpl implements ImpersonateOutletUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProviderPort jwtTokenProvider;
    private final OutletRepositoryPort outletPort;
    private final PermissionService permissionService;
    private final MasterTreeProvider masterTreeProvider;
    private final UserAvalonMapper userMapper;

    @Override
    public AuthResponse execute(Long outletId) {
        // 1. Obtener usuario autenticado actual
        String currentUsername = SecurityUtils.getCurrentUserLogin();
        if (currentUsername == null) {
            throw new BusinessException("No autenticado");
        }

        // 2. Verificar permisos de superadmin
        boolean hasAdminRole = SecurityUtils.hasRole("ROLE_ADMIN") || SecurityUtils.hasRole("ROLE_ADMINTI");
        if (!hasAdminRole) {
            throw new BusinessException("Solo los administradores globales pueden suplantar el acceso a tiendas");
        }

        // 3. Buscar tienda destino
        OutletDomain outlet = outletPort.findById(outletId)
                .orElseThrow(() -> new EntityNotFoundException("Tienda no encontrada"));

        // 4. Buscar entidad de usuario
        UserAvalonDomain user = userPort.findByIdentifier(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // 5. Cargar UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

        // 6. Generar JWT temporal con outletId destino y roles ["GERGEN", "ADMINTI"]
        List<String> impersonatedRoles = List.of("GERGEN", "ADMINTI");
        String impersonationToken = jwtTokenProvider.generateImpersonationToken(userDetails, outletId, impersonatedRoles);

        // 7. Generar Refresh Token
        UUID refreshTokenUuid = jwtTokenProvider.generateRefreshToken();

        // 8. Construir modos enriquecidos con GERGEN activo para la tienda destino
        ClientModeDto clientMode = new ClientModeDto(
                "ESTANDAR",
                true,
                List.of("AUTO_ASSIGN_CONSUMER_ROLE", "BUY_PRODUCTS", "VIEW_MARKETPLACE")
        );

        MasterRoot gergenRole = masterTreeProvider.getTree().getByCode("GERGEN");
        if (gergenRole == null) {
            throw new IllegalStateException("Rol GERGEN no encontrado en la jerarquía");
        }
        List<String> gergenPermissions = permissionService.resolvePermissions(gergenRole);

        OutletInfoDto outletDto = new OutletInfoDto(outlet.getId(), outlet.getName());

        EmployeeModeDto employeeMode = new EmployeeModeDto(
                true,
                outletDto,
                new MasterDataResponseDto(gergenRole.getId(), gergenRole.getShortName(), gergenRole.getFullName()),
                gergenPermissions
        );

        AdminAvalonModeDto adminMode = new AdminAvalonModeDto(
                true,
                outletDto,
                new MasterDataResponseDto(94L, "ADMINTI", "ADMIN_INFRAESTRUCTURA_TI"),
                List.of("FULL_ADMIN_ACCESS")
        );

        ModesResponseDto modes = new ModesResponseDto(clientMode, employeeMode, adminMode);

        // 9. Mapear respuesta de usuario
        MasterRoot status = masterTreeProvider.getTree().getById(user.getStatusId());
        UserAvalonResponseDto userDto = userMapper.toResponse(user, status);

        // 10. Devolver la respuesta de autenticación de suplantación
        return new AuthResponse(
                impersonationToken,
                refreshTokenUuid.toString(),
                userDto,
                modes
        );
    }
}
