package org.frias.avalon.domain.person.application.dto;

import org.frias.avalon.domain.person.application.dto.request.VerifyIdentificationRequestDto;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for Verification DTOs")
class VerifyIdentificationDtoTest {

    @Test
    @DisplayName("Should build and access VerifyIdentificationRequestDto correctly")
    void testVerifyIdentificationRequestDto() {
        VerifyIdentificationRequestDto request = new VerifyIdentificationRequestDto("12345678");
        assertEquals("12345678", request.identificationNumber());
    }

    @Test
    @DisplayName("Should build and access VerificationResponseDto correctly")
    void testVerificationResponseDto() {
        VerificationResponseDto response = new VerificationResponseDto(true, false, "JUAN PEREZ");
        assertTrue(response.personExists());
        assertFalse(response.userExists());
        assertEquals("JUAN PEREZ", response.nameHint());
    }
}
