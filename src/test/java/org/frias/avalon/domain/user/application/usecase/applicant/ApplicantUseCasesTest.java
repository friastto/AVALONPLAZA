package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantLookupRequestDto;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantSendPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantVerifyPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantLookupResponseDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantVerifiedResponseDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para Verificacion Segura de Solicitante de Empresa")
class ApplicantUseCasesTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private PasswordResetTokenRepositoryPort tokenRepositoryPort;

    @Mock
    private EmailServicePort emailServicePort;

    private ApplicantLookupUseCaseImpl lookupUseCase;
    private ApplicantSendPinUseCaseImpl sendPinUseCase;
    private ApplicantVerifyPinUseCaseImpl verifyPinUseCase;

    @BeforeEach
    void setUp() {
        lookupUseCase = new ApplicantLookupUseCaseImpl(personRepositoryPort, userAvalonRepositoryPort);
        sendPinUseCase = new ApplicantSendPinUseCaseImpl(personRepositoryPort, userAvalonRepositoryPort, tokenRepositoryPort, emailServicePort);
        verifyPinUseCase = new ApplicantVerifyPinUseCaseImpl(personRepositoryPort, userAvalonRepositoryPort, tokenRepositoryPort);
    }

    @Test
    @DisplayName("Deberia retornar userExists false si la persona no existe")
    void shouldReturnFalseWhenPersonDoesNotExist() {
        given(personRepositoryPort.findByNumberid("12345")).willReturn(Optional.empty());

        ApplicantLookupResponseDto response = lookupUseCase.execute(new ApplicantLookupRequestDto("12345"));

        assertFalse(response.userExists());
        assertNull(response.maskedEmail());
    }

    @Test
    @DisplayName("Deberia retornar userExists false si el usuario avalon no existe")
    void shouldReturnFalseWhenUserDoesNotExist() {
        PersonDomain person = PersonDomain.createBasic(1L, "12345", "Carlos", "Perez", "Dir", 1L, 300L, "carlos@test.com", 1L);
        given(personRepositoryPort.findByNumberid("12345")).willReturn(Optional.of(person));
        given(userAvalonRepositoryPort.findByPersonNumberid("12345")).willReturn(Optional.empty());
        given(userAvalonRepositoryPort.findByIdentifier("12345")).willReturn(Optional.empty());

        ApplicantLookupResponseDto response = lookupUseCase.execute(new ApplicantLookupRequestDto("12345"));

        assertFalse(response.userExists());
        assertNull(response.maskedEmail());
    }

    @Test
    @DisplayName("Deberia retornar userExists true y email enmascarado SIN revelar el nombre (Anti-fuga de datos)")
    void shouldReturnTrueAndMaskedEmailWithoutNameLeak() {
        PersonDomain person = PersonDomain.createBasic(1L, "12345", "Carlos", "Perez", "Dir", 1L, 300L, "carlosperez@gmail.com", 1L);
        UserAvalonDomain user = new UserAvalonDomain(10L, 1L, "carlos", "salt", "hash", 1L);

        given(personRepositoryPort.findByNumberid("12345")).willReturn(Optional.of(person));
        given(userAvalonRepositoryPort.findByPersonNumberid("12345")).willReturn(Optional.of(user));

        ApplicantLookupResponseDto response = lookupUseCase.execute(new ApplicantLookupRequestDto("12345"));

        assertTrue(response.userExists());
        assertNotNull(response.maskedEmail());
        assertTrue(response.maskedEmail().contains("@gmail.com"));
        assertTrue(response.maskedEmail().contains("***"));
    }

    @Test
    @DisplayName("Deberia generar PIN y enviarlo por correo al solicitante")
    void shouldGeneratePinAndSendEmail() {
        UserAvalonDomain user = new UserAvalonDomain(55L, 10L, "carlos", "salt", "hash", 1L);
        PersonDomain person = PersonDomain.createFromEntity(10L, "12345", "Carlos", "Perez", "Dir", 1L, 1L, 300L, "carlos@empresa.com", 1L, null, null);

        given(userAvalonRepositoryPort.findByPersonNumberid("12345")).willReturn(Optional.of(user));
        given(personRepositoryPort.findById(10L)).willReturn(Optional.of(person));

        sendPinUseCase.execute(new ApplicantSendPinRequestDto("12345"));

        verify(tokenRepositoryPort).save(any(PasswordResetTokenDomain.class));
        verify(emailServicePort).sendApplicantVerificationPin(eq("carlos@empresa.com"), anyString());
    }

    @Test
    @DisplayName("Deberia validar PIN exitosamente y revelar identidad confirmada")
    void shouldVerifyPinSuccessfullyAndReturnIdentity() {
        UserAvalonDomain user = new UserAvalonDomain(55L, 10L, "carlos", "salt", "hash", 1L);
        PersonDomain person = PersonDomain.createFromEntity(10L, "12345", "Carlos", "Perez", "Dir", 1L, 1L, 300L, "carlos@empresa.com", 1L, null, null);

        PasswordResetTokenDomain token = PasswordResetTokenDomain.create(55L);

        given(userAvalonRepositoryPort.findByPersonNumberid("12345")).willReturn(Optional.of(user));
        given(tokenRepositoryPort.findByUserIdAndPin(55L, token.getPin())).willReturn(Optional.of(token));
        given(personRepositoryPort.findById(10L)).willReturn(Optional.of(person));

        ApplicantVerifiedResponseDto response = verifyPinUseCase.execute(
                new ApplicantVerifyPinRequestDto("12345", token.getPin())
        );

        assertNotNull(response);
        assertEquals(55L, response.userId());
        assertEquals(10L, response.personId());
        assertEquals("Carlos Perez", response.fullName());
        assertEquals("carlos@empresa.com", response.email());
        assertNotNull(response.verificationToken());
    }

    @Test
    @DisplayName("Deberia fallar si el PIN es incorrecto")
    void shouldThrowExceptionWhenPinIsInvalid() {
        UserAvalonDomain user = new UserAvalonDomain(55L, 10L, "carlos", "salt", "hash", 1L);

        given(userAvalonRepositoryPort.findByPersonNumberid("12345")).willReturn(Optional.of(user));
        given(tokenRepositoryPort.findByUserIdAndPin(55L, "999999")).willReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                verifyPinUseCase.execute(new ApplicantVerifyPinRequestDto("12345", "999999"))
        );
    }
}
