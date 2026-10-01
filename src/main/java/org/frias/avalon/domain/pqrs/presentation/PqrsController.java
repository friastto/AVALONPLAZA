package org.frias.avalon.domain.pqrs.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.pqrs.application.dto.request.CreatePqrsRequest;
import org.frias.avalon.domain.pqrs.application.dto.request.UpdatePqrsStatusRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.usecase.CreatePqrsUseCase;
import org.frias.avalon.domain.pqrs.application.usecase.FindPqrsUseCase;
import org.frias.avalon.domain.pqrs.application.usecase.UpdatePqrsStatusUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/avalon/pqrs", "/api/v1/pqrs"})
@RequiredArgsConstructor
public class PqrsController {

    private final CreatePqrsUseCase createPqrsUseCase;
    private final FindPqrsUseCase findPqrsUseCase;
    private final UpdatePqrsStatusUseCase updatePqrsStatusUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<PqrsResponse>> createPqrs(@Valid @RequestBody CreatePqrsRequest request) {
        PqrsResponse response = createPqrsUseCase.execute(request);
        ApiResponse<PqrsResponse> apiResponse = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "PQRS radicado exitosamente con numero " + response.getTicketNumber(),
                response
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PqrsResponse>>> getAllPqrs(
            @RequestParam(required = false) String statusCode,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PqrsResponse> page = findPqrsUseCase.findAll(statusCode, typeCode, search, pageable);
        ApiResponse<Page<PqrsResponse>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Listado de PQRS obtenido exitosamente",
                page
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = findPqrsUseCase.getPqrsStats();
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Estadisticas de PQRS obtenidas exitosamente",
                stats
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PqrsResponse>> getById(@PathVariable Long id) {
        PqrsResponse response = findPqrsUseCase.findById(id);
        ApiResponse<PqrsResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Detalle de PQRS obtenido exitosamente",
                response
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PqrsResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePqrsStatusRequest request
    ) {
        PqrsResponse response = updatePqrsStatusUseCase.execute(id, request);
        ApiResponse<PqrsResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Estado de PQRS actualizado exitosamente",
                response
        );
        return ResponseEntity.ok(apiResponse);
    }
}
