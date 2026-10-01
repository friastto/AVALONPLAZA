package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponseMapper;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindPqrsUseCaseImplTest {

    @Mock
    private PqrsRepositoryPort pqrsRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    private PqrsResponseMapper pqrsResponseMapper;
    private FindPqrsUseCaseImpl findPqrsUseCase;

    @BeforeEach
    void setUp() {
        pqrsResponseMapper = new PqrsResponseMapper();
        findPqrsUseCase = new FindPqrsUseCaseImpl(pqrsRepositoryPort, userAvalonRepositoryPort, pqrsResponseMapper);
    }

    @Test
    void shouldFindAllPqrs() {
        Pageable pageable = PageRequest.of(0, 10);
        PqrsDomain domain = PqrsDomain.builder()
                .id(1L)
                .ticketNumber("PQRS-20261001-11111")
                .typeCode("PET")
                .statusCode("PEN")
                .subject("Consulta")
                .description("Detalle de consulta")
                .build();

        Page<PqrsDomain> page = new PageImpl<>(Collections.singletonList(domain), pageable, 1);
        when(pqrsRepositoryPort.findAll(null, null, null, pageable)).thenReturn(page);

        Page<PqrsResponse> result = findPqrsUseCase.findAll(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PQRS-20261001-11111", result.getContent().get(0).getTicketNumber());
    }

    @Test
    void shouldFindMyPqrsWhenUserAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("juanperez", "pass")
        );

        UserAvalonDomain user = new UserAvalonDomain(5L, "juanperez", 1L);

        when(userAvalonRepositoryPort.findByUserName("juanperez")).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        PqrsDomain domain = PqrsDomain.builder()
                .id(2L)
                .ticketNumber("PQRS-20261001-22222")
                .userId(5L)
                .typeCode("FEL")
                .statusCode("RES")
                .subject("Felicitaciones por el servicio")
                .description("Excelente atencion al cliente")
                .adminNotes("Muchas gracias por sus comentarios")
                .build();

        Page<PqrsDomain> page = new PageImpl<>(Collections.singletonList(domain), pageable, 1);
        when(pqrsRepositoryPort.findByUserId(5L, pageable)).thenReturn(page);

        Page<PqrsResponse> result = findPqrsUseCase.findMyPqrs(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Felicitacion", result.getContent().get(0).getTypeName());
        assertEquals("RES", result.getContent().get(0).getStatusCode());
        assertEquals("Muchas gracias por sus comentarios", result.getContent().get(0).getAdminNotes());

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetPqrsStats() {
        when(pqrsRepositoryPort.countByStatusCode("PEN")).thenReturn(5L);
        when(pqrsRepositoryPort.countByStatusCode("PRO")).thenReturn(2L);
        when(pqrsRepositoryPort.countByStatusCode("RES")).thenReturn(10L);
        when(pqrsRepositoryPort.countByStatusCode("CER")).thenReturn(1L);

        Map<String, Object> stats = findPqrsUseCase.getPqrsStats();

        assertEquals(5L, stats.get("pending"));
        assertEquals(2L, stats.get("inProgress"));
        assertEquals(10L, stats.get("resolved"));
        assertEquals(1L, stats.get("closed"));
        assertEquals(18L, stats.get("total"));
    }
}
