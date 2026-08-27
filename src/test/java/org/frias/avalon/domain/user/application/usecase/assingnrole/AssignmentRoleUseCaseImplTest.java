package org.frias.avalon.domain.user.application.usecase.assingnrole;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.service.PermissionService;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.RoleAssignmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for AssignmentRoleUseCaseImpl in RBAC Domain")
class AssignmentRoleUseCaseImplTest {

    private UserAvalonRepositoryPort userPort;
    private RoleAssignmentRepositoryPort rolePort;
    private MasterDataRepositoryPort masterPort;
    private RoleAssignmentMapper mapper;
    private MasterTreeProvider treeProvider;
    private OutletRepositoryPort outletRepositoryPort;
    private PermissionService permissionService;

    private AssignmentRoleUseCaseImpl assignmentRoleUseCase;

    @BeforeEach
    void setUp() {
        userPort = mock(UserAvalonRepositoryPort.class);
        rolePort = mock(RoleAssignmentRepositoryPort.class);
        masterPort = mock(MasterDataRepositoryPort.class);
        mapper = mock(RoleAssignmentMapper.class);
        treeProvider = mock(MasterTreeProvider.class);
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        permissionService = mock(PermissionService.class);

        assignmentRoleUseCase = new AssignmentRoleUseCaseImpl(
                userPort, rolePort, masterPort, mapper, treeProvider, outletRepositoryPort, permissionService
        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found")
    void shouldThrowExceptionWhenUserNotFound() {
        AssignmentRoleRequestDto request = new AssignmentRoleRequestDto(99L, 5L, 1L);
        when(treeProvider.getTree()).thenReturn(mock(MasterTree.class));
        when(userPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> assignmentRoleUseCase.execute(request));
    }

    @Test
    @DisplayName("Should assign role successfully when permissions and rules pass")
    void shouldAssignRoleSuccessfully() {
        AssignmentRoleRequestDto request = new AssignmentRoleRequestDto(10L, 5L, 1L);

        MasterTree tree = mock(MasterTree.class);
        when(treeProvider.getTree()).thenReturn(tree);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(10L, 100L, "operator1", 1L);
        when(userPort.findById(10L)).thenReturn(Optional.of(user));

        MasterRoot userStatus = new MasterRoot(1L, "ACT", "Activo", 0L, 1L);
        MasterRoot roleNode = new MasterRoot(5L, "CJTURNO", "Cajero de Turno", 0L, 1L);
        MasterRoot activeStatus = new MasterRoot(1L, "ACT", "Activo", 0L, 1L);

        when(masterPort.findById(1L)).thenReturn(Optional.of(userStatus));
        when(masterPort.findById(5L)).thenReturn(Optional.of(roleNode));
        when(masterPort.getActiveStatus()).thenReturn(Optional.of(activeStatus));

        when(tree.isChildOf(roleNode, "ROL")).thenReturn(true);
        when(tree.isChildOf(roleNode, "CONS")).thenReturn(false);
        when(rolePort.findByUserAvalonId(10L)).thenReturn(List.of());

        when(permissionService.canAssignRole(roleNode, 1L)).thenReturn(true);

        OutletDomain outlet = new OutletDomain(
                1L, "OUT-1", "Tienda 1", "Calle 1", "5551234", "900123456", 1L, null,
                new BigDecimal("500000"), true, new BigDecimal("3000"), 2L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(outletRepositoryPort.findById(1L)).thenReturn(Optional.of(outlet));
        when(tree.getById(1L)).thenReturn(activeStatus);

        StatusResponseDto statusDto = new StatusResponseDto(1L, "ACT", "Activo");
        UserAvalonResponseDto userDto = new UserAvalonResponseDto(10L, 100L, "operator1", statusDto);
        MasterDataResponseDto roleDto = new MasterDataResponseDto(5L, "CJTURNO", "Cajero de Turno", null, null);

        AssignmentRoleResponse expectedResponse = new AssignmentRoleResponse(userDto, roleDto, 1L, statusDto);
        when(mapper.toResponse(any(), any(), any(), any(), any())).thenReturn(expectedResponse);

        AssignmentRoleResponse response = assignmentRoleUseCase.execute(request);

        assertNotNull(response);
        assertEquals("CJTURNO", response.role().shortName());
        verify(rolePort, times(1)).create(any(RoleAssignmentDomain.class));
    }
}
