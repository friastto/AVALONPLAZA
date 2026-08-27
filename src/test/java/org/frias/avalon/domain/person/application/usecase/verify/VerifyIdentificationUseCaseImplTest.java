package org.frias.avalon.domain.person.application.usecase.verify;

import org.frias.avalon.domain.person.application.dto.request.VerifyIdentificationRequestDto;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for VerifyIdentificationUseCaseImpl")
class VerifyIdentificationUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    private VerifyIdentificationUseCaseImpl useCase;

    private final String numberid = "12345678";

    @BeforeEach
    void setUp() {
        useCase = new VerifyIdentificationUseCaseImpl(personRepositoryPort, userAvalonRepositoryPort);
    }

    private PersonDomain createSamplePerson() {
        return PersonDomain.createFromEntity(
                1L, numberid, "PEDRO", "GOMEZ", "CALLE 10",
                1L, 1L, 5551111L, "pedro@email.com", 1L,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Execute Verification Scenarios")
    class ExecuteScenarios {

        @Test
        @DisplayName("Should return personExists=false and userExists=false when person is not found")
        void shouldReturnFalseWhenPersonNotFound() {
            VerifyIdentificationRequestDto request = new VerifyIdentificationRequestDto(numberid);
            when(personRepositoryPort.findByNumberid(numberid)).thenReturn(Optional.empty());

            VerificationResponseDto response = useCase.execute(request);

            assertNotNull(response);
            assertFalse(response.personExists());
            assertFalse(response.userExists());
            assertNull(response.nameHint());
        }

        @Test
        @DisplayName("Should return personExists=true and userExists=true when person and user exist")
        void shouldReturnTrueWhenPersonAndUserExist() {
            VerifyIdentificationRequestDto request = new VerifyIdentificationRequestDto(numberid);
            PersonDomain person = createSamplePerson();
            when(personRepositoryPort.findByNumberid(numberid)).thenReturn(Optional.of(person));

            UserAvalonDomain user = mock(UserAvalonDomain.class);
            when(userAvalonRepositoryPort.findByIdentifier(numberid)).thenReturn(Optional.of(user));

            VerificationResponseDto response = useCase.execute(request);

            assertNotNull(response);
            assertTrue(response.personExists());
            assertTrue(response.userExists());
            assertNull(response.nameHint());
        }

        @Test
        @DisplayName("Should return personExists=true and userExists=false when person exists but user does not")
        void shouldReturnTrueForPersonAndFalseForUserWhenUserMissing() {
            VerifyIdentificationRequestDto request = new VerifyIdentificationRequestDto(numberid);
            PersonDomain person = createSamplePerson();
            when(personRepositoryPort.findByNumberid(numberid)).thenReturn(Optional.of(person));
            when(userAvalonRepositoryPort.findByIdentifier(numberid)).thenReturn(Optional.empty());

            VerificationResponseDto response = useCase.execute(request);

            assertNotNull(response);
            assertTrue(response.personExists());
            assertFalse(response.userExists());
            assertNull(response.nameHint());
        }
    }
}
