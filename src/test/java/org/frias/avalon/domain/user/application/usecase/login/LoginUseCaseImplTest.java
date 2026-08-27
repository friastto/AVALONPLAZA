package org.frias.avalon.domain.user.application.usecase.login;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.jwt.config.CustomUserDetailsService;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.service.BuildAuthenticationResponse;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for LoginUseCaseImpl")
class LoginUseCaseImplTest {

    @Mock
    private UserAvalonRepositoryPort userPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private BuildAuthenticationResponse buildAuthenticationResponse;

    @Mock
    private MasterTree masterTree;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private String rawPassword;
    private String salt;
    private String hashPassword;
    private UserAvalonDomain validUser;
    private MasterRoot activeStatus;

    @BeforeEach
    void setUp() {
        rawPassword = "SecretPassword123!";
        salt = PassSecure.generateSalt();
        hashPassword = PassSecure.hashPassword(rawPassword, salt);

        validUser = UserAvalonDomain.fromPersistenceAdvanced(
                1L, 10L, "johndoe", salt, hashPassword, 1L
        );
        activeStatus = new MasterRoot(1L, "ACT", "Activo", 0L, 1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found by identifier")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        AuthRequest request = new AuthRequest("unknownUser", rawPassword);
        when(userPort.findByIdentifier("unknownUser")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> loginUseCase.execute(request));

        assertEquals("usuario no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when status is missing from MasterTree cache")
    void shouldThrowIllegalStateExceptionWhenStatusNotInTree() {
        AuthRequest request = new AuthRequest("johndoe", rawPassword);
        when(userPort.findByIdentifier("johndoe")).thenReturn(Optional.of(validUser));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loginUseCase.execute(request));

        assertEquals("Estado inconsistente en cache", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when password verification fails")
    void shouldThrowIllegalStateExceptionWhenPasswordIsInvalid() {
        AuthRequest request = new AuthRequest("johndoe", "WrongPassword123!");
        when(userPort.findByIdentifier("johndoe")).thenReturn(Optional.of(validUser));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(activeStatus);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loginUseCase.execute(request));

        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user account is disabled")
    void shouldThrowIllegalStateExceptionWhenAccountDisabled() {
        AuthRequest request = new AuthRequest("johndoe", rawPassword);
        when(userPort.findByIdentifier("johndoe")).thenReturn(Optional.of(validUser));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(activeStatus);
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loginUseCase.execute(request));

        assertEquals("La cuenta del usuario no está activa o está bloqueada.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user account is locked")
    void shouldThrowIllegalStateExceptionWhenAccountLocked() {
        AuthRequest request = new AuthRequest("johndoe", rawPassword);
        when(userPort.findByIdentifier("johndoe")).thenReturn(Optional.of(validUser));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(activeStatus);
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loginUseCase.execute(request));

        assertEquals("La cuenta del usuario no está activa o está bloqueada.", ex.getMessage());
    }

    @Test
    @DisplayName("Should successfully authenticate user and return AuthResponse")
    void shouldSuccessfullyAuthenticateUser() {
        AuthRequest request = new AuthRequest("johndoe", rawPassword);
        when(userPort.findByIdentifier("johndoe")).thenReturn(Optional.of(validUser));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(activeStatus);
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);

        AuthResponse expectedResponse = new AuthResponse("acc-token", "ref-token", mock(UserAvalonResponseDto.class), null);
        when(buildAuthenticationResponse.buildAuthenticationResponse(validUser, userDetails)).thenReturn(expectedResponse);

        AuthResponse actualResponse = loginUseCase.execute(request);

        assertNotNull(actualResponse);
        assertEquals("acc-token", actualResponse.accessToken());
        assertEquals("ref-token", actualResponse.refreshToken());
        verify(buildAuthenticationResponse).buildAuthenticationResponse(validUser, userDetails);
    }
}
