package org.frias.avalon.domain.inventory.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.inventory.application.dto.ApproveAuditSessionRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditItemCountRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditItemDto;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.CreateAuditSessionRequest;
import org.frias.avalon.domain.inventory.application.usecase.ApproveAuditSessionUseCase;
import org.frias.avalon.domain.inventory.application.usecase.CreateAuditSessionUseCase;
import org.frias.avalon.domain.inventory.application.usecase.GetAuditSessionDetailUseCase;
import org.frias.avalon.domain.inventory.application.usecase.RecordAuditCountUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/avalon/inventory/audit", "/api/v1/inventory/audit"})
public class InventoryAuditController {

    private final CreateAuditSessionUseCase createAuditSessionUseCase;
    private final RecordAuditCountUseCase recordAuditCountUseCase;
    private final ApproveAuditSessionUseCase approveAuditSessionUseCase;
    private final GetAuditSessionDetailUseCase getAuditSessionDetailUseCase;

    public InventoryAuditController(
            CreateAuditSessionUseCase createAuditSessionUseCase,
            RecordAuditCountUseCase recordAuditCountUseCase,
            ApproveAuditSessionUseCase approveAuditSessionUseCase,
            GetAuditSessionDetailUseCase getAuditSessionDetailUseCase
    ) {
        this.createAuditSessionUseCase = createAuditSessionUseCase;
        this.recordAuditCountUseCase = recordAuditCountUseCase;
        this.approveAuditSessionUseCase = approveAuditSessionUseCase;
        this.getAuditSessionDetailUseCase = getAuditSessionDetailUseCase;
    }

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<AuditSessionDetailResponse>> createSession(
            @Valid @RequestBody CreateAuditSessionRequest request
    ) {
        AuditSessionDetailResponse response = createAuditSessionUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Sesion de auditoria creada o reanudada exitosamente",
                response
        ));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<AuditSessionDetailResponse>> getSessionById(
            @PathVariable Long sessionId
    ) {
        AuditSessionDetailResponse response = getAuditSessionDetailUseCase.findById(sessionId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Detalle de sesion de auditoria obtenido exitosamente",
                response
        ));
    }

    @GetMapping("/outlet/{outletId}/active")
    public ResponseEntity<ApiResponse<AuditSessionDetailResponse>> getActiveSessionByOutlet(
            @PathVariable Long outletId
    ) {
        AuditSessionDetailResponse response = getAuditSessionDetailUseCase.findActiveByOutletId(outletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                response != null ? "Sesion activa encontrada" : "No hay sesion de auditoria activa para esta tienda",
                response
        ));
    }

    @PostMapping("/count")
    public ResponseEntity<ApiResponse<AuditItemDto>> recordCount(
            @Valid @RequestBody AuditItemCountRequest request
    ) {
        AuditItemDto response = recordAuditCountUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Conteo de producto registrado exitosamente",
                response
        ));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<AuditSessionDetailResponse>> approveSession(
            @Valid @RequestBody ApproveAuditSessionRequest request
    ) {
        AuditSessionDetailResponse response = approveAuditSessionUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Auditoria aprobada, stock reajustado en Kardex y sesion completada",
                response
        ));
    }
}
