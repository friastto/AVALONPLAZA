package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.request.UpdatePqrsStatusRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponseMapper;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePqrsStatusUseCaseImplTest {

    @Mock
    private PqrsRepositoryPort pqrsRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    private PqrsResponseMapper pqrsResponseMapper;
    private UpdatePqrsStatusUseCaseImpl updatePqrsStatusUseCase;

    @BeforeEach
    void setUp() {
        pqrsResponseMapper = new PqrsResponseMapper();
        updatePqrsStatusUseCase = new UpdatePqrsStatusUseCaseImpl(pqrsRepositoryPort, userAvalonRepositoryPort, pqrsResponseMapper);
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        PqrsDomain existing = PqrsDomain.builder()
                .id(1L)
                .ticketNumber("PQRS-20261001-12345")
                .typeCode("REC")
                .statusCode("PEN")
                .subject("Demora")
                .description("Descripcion larga")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(pqrsRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(pqrsRepositoryPort.save(any(PqrsDomain.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePqrsStatusRequest request = UpdatePqrsStatusRequest.builder()
                .statusCode("RES")
                .adminNotes("Caso resuelto favorablemente y compensado con cupon.")
                .build();

        PqrsResponse response = updatePqrsStatusUseCase.execute(1L, request);

        assertNotNull(response);
        assertEquals("RES", response.getStatusCode());
        assertEquals("Resuelto", response.getStatusName());
        assertEquals("Caso resuelto favorablemente y compensado con cupon.", response.getAdminNotes());
        verify(pqrsRepositoryPort).save(existing);
    }

    @Test
    void shouldThrowExceptionWhenPqrsNotFound() {
        when(pqrsRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        UpdatePqrsStatusRequest request = UpdatePqrsStatusRequest.builder()
                .statusCode("RES")
                .build();

        assertThrows(NoSuchElementException.class, () -> updatePqrsStatusUseCase.execute(999L, request));
    }
}
