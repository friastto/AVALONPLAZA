package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.service.BuildAuthenticationResponse;
import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RefreshTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for GenerateAccessTokenAndRefreshTokenUseCaseImpl")
class GenerateAccessTokenAndRefreshTokenUseCaseImplTest {

    @Mock
    private RefreshTokenRepositoryPort refreshTokenPort;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private BuildAuthenticationResponse buildAuthenticationResponse;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private GenerateAccessTokenAndRefreshTokenUseCaseImpl generateAccessTokenAndRefreshTokenUseCase;

    private String validTokenStr;
    private Long userAvalonId;
    private UserAvalonDomain userDomain;

    @BeforeEach
    void setUp() {
        validTokenStr = UUID.randomUUID().toString();
        userAvalonId = 15L;
        userDomain = UserAvalonDomain.fromPersistenceBasic(userAvalonId, 100L, "testuser", 1L);
    }

    @Test
    @DisplayName("Should throw RuntimeException when refresh token does not exist in repository")
    void shouldThrowRuntimeExceptionWhenTokenNotFound() {
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("El Refresh Token proporcionado no es válido.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException when refresh token is already revoked")
    void shouldThrowRuntimeExceptionWhenTokenIsRevoked() {
        RefreshTokenDomain revokedToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().plusSeconds(3600), true, Instant.now()
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(revokedToken));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("Este Refresh Token ya ha sido revocado. Acceso denegado.", ex.getMessage());
    }

    @Test
    @DisplayName("Should delete expired refresh token and throw RuntimeException")
    void shouldDeleteTokenAndThrowRuntimeExceptionWhenExpired() {
        RefreshTokenDomain expiredToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().minusSeconds(100), false, Instant.now().minusSeconds(3600)
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(expiredToken));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("El Refresh Token ha expirado. Por favor, inicie sesión nuevamente.", ex.getMessage());
        verify(refreshTokenPort, times(1)).deleteByRefreshToken(validTokenStr);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not found for refresh token")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        RefreshTokenDomain validToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().plusSeconds(3600), false, Instant.now()
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(validToken));
        when(userAvalonRepositoryPort.findById(userAvalonId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("usuario no encontrado para el rftokn ", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user account is disabled")
    void shouldThrowIllegalStateExceptionWhenUserDisabled() {
        RefreshTokenDomain validToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().plusSeconds(3600), false, Instant.now()
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(validToken));
        when(userAvalonRepositoryPort.findById(userAvalonId)).thenReturn(Optional.of(userDomain));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("La cuenta del usuario no está activa o está bloqueada.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user account is locked")
    void shouldThrowIllegalStateExceptionWhenUserLocked() {
        RefreshTokenDomain validToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().plusSeconds(3600), false, Instant.now()
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(validToken));
        when(userAvalonRepositoryPort.findById(userAvalonId)).thenReturn(Optional.of(userDomain));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr));

        assertEquals("La cuenta del usuario no está activa o está bloqueada.", ex.getMessage());
    }

    @Test
    @DisplayName("Should rotate refresh token, revoke old token, save it, and return AuthResponse")
    void shouldRotateRefreshTokenAndReturnAuthResponse() {
        RefreshTokenDomain validToken = new RefreshTokenDomain(
                UUID.randomUUID(), validTokenStr, userAvalonId, Instant.now().plusSeconds(3600), false, Instant.now()
        );
        when(refreshTokenPort.findByRefreshToken(validTokenStr)).thenReturn(Optional.of(validToken));
        when(userAvalonRepositoryPort.findById(userAvalonId)).thenReturn(Optional.of(userDomain));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);

        AuthResponse expectedAuthResponse = new AuthResponse("new-access-token", "new-refresh-token", mock(UserAvalonResponseDto.class), null);
        when(buildAuthenticationResponse.buildAuthenticationResponse(userDomain, userDetails)).thenReturn(expectedAuthResponse);

        AuthResponse actualResponse = generateAccessTokenAndRefreshTokenUseCase.execute(validTokenStr);

        assertNotNull(actualResponse);
        assertEquals("new-access-token", actualResponse.accessToken());
        assertEquals("new-refresh-token", actualResponse.refreshToken());

        ArgumentCaptor<RefreshTokenDomain> tokenCaptor = ArgumentCaptor.forClass(RefreshTokenDomain.class);
        verify(refreshTokenPort).save(tokenCaptor.capture());

        assertTrue(tokenCaptor.getValue().isRevoked(), "The old refresh token should be revoked during rotation.");
        verify(buildAuthenticationResponse).buildAuthenticationResponse(userDomain, userDetails);
    }
}
