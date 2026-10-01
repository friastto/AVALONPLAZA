package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.request.CreatePqrsRequest;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePqrsUseCaseImplTest {

    @Mock
    private PqrsRepositoryPort pqrsRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    private PqrsResponseMapper pqrsResponseMapper;
    private CreatePqrsUseCaseImpl createPqrsUseCase;

    @BeforeEach
    void setUp() {
        pqrsResponseMapper = new PqrsResponseMapper();
        createPqrsUseCase = new CreatePqrsUseCaseImpl(pqrsRepositoryPort, userAvalonRepositoryPort, pqrsResponseMapper);
    }

    @Test
    void shouldCreatePqrsSuccessfully() {
        CreatePqrsRequest request = CreatePqrsRequest.builder()
                .typeCode("RECLAMO")
                .subject("Demora en el despacho")
                .description("Mi pedido tardo mas de una hora en llegar.")
                .contactEmail("test@example.com")
                .contactPhone("3001234567")
                .orderId(10L)
                .build();

        when(pqrsRepositoryPort.findByTicketNumber(anyString())).thenReturn(Optional.empty());
        when(pqrsRepositoryPort.save(any(PqrsDomain.class))).thenAnswer(invocation -> {
            PqrsDomain arg = invocation.getArgument(0);
            arg.setId(100L);
            return arg;
        });

        PqrsResponse response = createPqrsUseCase.execute(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertTrue(response.getTicketNumber().startsWith("PQRS-"));
        assertEquals("REC", response.getTypeCode());
        assertEquals("Reclamo", response.getTypeName());
        assertEquals("PEN", response.getStatusCode());
        assertEquals("Pendiente", response.getStatusName());
        assertEquals("Demora en el despacho", response.getSubject());
        verify(pqrsRepositoryPort, times(1)).save(any(PqrsDomain.class));
    }
}
