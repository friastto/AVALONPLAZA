package org.frias.avalon.domain.user.application.usecase.changestatus;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.core.permissions.validchangestatus.StatusChangeValidator;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ChangeStatusUserAvalonUseCase")
class ChangeStatusUserAvalonUseCaseTest {

    @Mock
    private UserAvalonRepositoryPort userAvalonPort;
    @Mock
    private MasterDataRepositoryPort masterDataPort;
    @Mock
    private StatusChangeValidator statusChangeValidator;
    @Mock
    private UserAvalonMapper userAvalonMapper;
    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentPort;
    @Mock
    private MasterTreeProvider masterTreeProvider;
    @Mock
    private CurrentUserProviderPort currentUserProvider;

    private ChangeStatusUserAvalonUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ChangeStatusUserAvalonUseCaseImpl(
                userAvalonPort,
                masterDataPort,
                statusChangeValidator,
                userAvalonMapper,
                roleAssignmentPort,
                masterTreeProvider,
                currentUserProvider
        );
    }

    @Test
    @DisplayName("Debería cambiar el estado exitosamente si el ejecutor y el objetivo pertenecen al mismo outlet")
    void shouldChangeStatusSuccessfullyWhenSameOutlet() {
        // Arrange
        Long targetUserId = 2L;
        Long activeStatusId = 1L;
        Long newStatusId = 4L; // INA
        Long outletId = 10L;

        ChangeUserAvalonStatusRequest request = new ChangeUserAvalonStatusRequest(targetUserId, "INA");

        UserAvalonDomain targetUser = UserAvalonDomain.fromPersistenceBasic(targetUserId, 1L, "targetUser", activeStatusId);
        
        MasterRoot oldStatus = mock(MasterRoot.class);
        when(oldStatus.getShortName()).thenReturn("ACT");
        MasterRoot newStatus = mock(MasterRoot.class);
        when(newStatus.getShortName()).thenReturn("INA");
        when(newStatus.getId()).thenReturn(newStatusId);

        when(userAvalonPort.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(masterDataPort.findById(activeStatusId)).thenReturn(Optional.of(oldStatus));
        when(masterDataPort.findByCode("INA")).thenReturn(Optional.of(newStatus));

        UserContext executorContext = new UserContext("executor", List.of("ROLE_GERGEN"), outletId);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(executorContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(outletId);

        RoleAssignmentDomain assignment = mock(RoleAssignmentDomain.class);
        when(assignment.getOutletId()).thenReturn(outletId);
        when(assignment.getRoleId()).thenReturn(100L); // ID del rol objetivo
        when(roleAssignmentPort.findByUserAvalonId(targetUserId)).thenReturn(List.of(assignment));

        MasterTree tree = mock(MasterTree.class);
        MasterRoot targetRoleNode = mock(MasterRoot.class);
        when(targetRoleNode.getShortName()).thenReturn("CJTURNO");
        when(tree.getById(100L)).thenReturn(targetRoleNode);
        when(masterTreeProvider.getTree()).thenReturn(tree);

        when(statusChangeValidator.validate(executorContext, "CJTURNO", outletId, "INA", null, false))
                .thenReturn(true);

        when(userAvalonPort.save(any(UserAvalonDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleAssignmentPort.create(any(RoleAssignmentDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(request);

        // Assert
        verify(userAvalonPort).save(argThat(user -> user.getStatusId().equals(newStatusId)));
        verify(roleAssignmentPort).create(assignment);
        verify(assignment).changeStatus(newStatusId);
    }

    @Test
    @DisplayName("Debería lanzar BusinessException si el usuario objetivo no pertenece al mismo outlet que el ejecutor")
    void shouldThrowExceptionWhenDifferentOutlet() {
        // Arrange
        Long targetUserId = 2L;
        Long activeStatusId = 1L;
        Long newStatusId = 4L;
        Long executorOutletId = 10L;
        Long targetOutletId = 20L;

        ChangeUserAvalonStatusRequest request = new ChangeUserAvalonStatusRequest(targetUserId, "INA");

        UserAvalonDomain targetUser = UserAvalonDomain.fromPersistenceBasic(targetUserId, 1L, "targetUser", activeStatusId);
        
        MasterRoot oldStatus = mock(MasterRoot.class);
        when(oldStatus.getShortName()).thenReturn("ACT");
        MasterRoot newStatus = mock(MasterRoot.class);
        when(newStatus.getShortName()).thenReturn("INA");

        when(userAvalonPort.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(masterDataPort.findById(activeStatusId)).thenReturn(Optional.of(oldStatus));
        when(masterDataPort.findByCode("INA")).thenReturn(Optional.of(newStatus));

        UserContext executorContext = new UserContext("executor", List.of("ROLE_GERGEN"), executorOutletId);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(executorContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(executorOutletId);

        RoleAssignmentDomain assignment = mock(RoleAssignmentDomain.class);
        when(assignment.getOutletId()).thenReturn(targetOutletId); // Pertenece a otra tienda
        when(roleAssignmentPort.findByUserAvalonId(targetUserId)).thenReturn(List.of(assignment));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            useCase.execute(request);
        });
        assertEquals("Acceso denegado: El usuario objetivo no pertenece a su misma tienda.", exception.getMessage());
        verify(userAvalonPort, never()).save(any());
        verify(roleAssignmentPort, never()).create(any());
    }
}
