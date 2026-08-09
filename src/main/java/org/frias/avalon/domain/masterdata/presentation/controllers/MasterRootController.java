package org.frias.avalon.domain.masterdata.presentation.controllers;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.usecase.changestatus.ChangeStatusUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.delete.DeleteMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindMasterDataByIdUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindMasterDataChildrenByParentCodeUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.reparent.ReparentMasterDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avalon/masterdata")
public class MasterRootController {
    private final CreateMasterDataUseCase createUseCase;
    private final FindMasterDataByIdUseCase findByIdUseCase;
    private final ChangeStatusUseCase changeStatusUseCase;
    private final CreateAllMasterDataUseCase createAllMasterDataUseCase;
    private final FindAllMasterDataUseCase findAllUseCase;
    private final DeleteMasterDataUseCase deleteUseCase;
    private final FindMasterDataChildrenByParentCodeUseCase findChildrenUseCase;
    private final ReparentMasterDataUseCase reparentUseCase;


    public MasterRootController(CreateMasterDataUseCase createUseCase, FindMasterDataByIdUseCase findByIdUseCase, ChangeStatusUseCase changeStatusUseCase, CreateAllMasterDataUseCase createAllMasterDataUseCase, FindAllMasterDataUseCase findAllUseCase, DeleteMasterDataUseCase deleteUseCase, FindMasterDataChildrenByParentCodeUseCase findChildrenUseCase, ReparentMasterDataUseCase reparentUseCase) {
        this.createUseCase = createUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.changeStatusUseCase = changeStatusUseCase;
        this.createAllMasterDataUseCase = createAllMasterDataUseCase;
        this.findAllUseCase = findAllUseCase;
        this.deleteUseCase = deleteUseCase;
        this.findChildrenUseCase = findChildrenUseCase;
        this.reparentUseCase = reparentUseCase;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMINTI') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> create(@Valid @RequestBody MasterDataNewDto request) {
        Long id = createUseCase.execute(request);
        MasterDataResponseDto response = findByIdUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(201, "se creo el tipo exitosamente", response));
    }

    @GetMapping("/showAll")
    public ResponseEntity<ApiResponse<List<MasterDataResponseDto>>> showAll() {
        List<MasterDataResponseDto> response = findAllUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "se listaron los datos maestros ", response));
    }

    @GetMapping("/search/v2/{id}")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> searchById(@PathVariable Long id) {
        MasterDataResponseDto response = findByIdUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "", response));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMINTI') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> delete(@PathVariable Long id) {
        MasterDataResponseDto deletedData = deleteUseCase.execute(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Registro eliminado exitosamente", deletedData));
    }

    @PatchMapping("/change/status")
    @PreAuthorize("hasAuthority('ADMINTI') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> updateById(@RequestBody MasterDataUpdateStatusDto dataDto) {
        MasterDataResponseDto response = changeStatusUseCase.execute(dataDto);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "Se actualiso el estado", response));
    }

    @PostMapping("/save/all")
    @PreAuthorize("hasAuthority('ADMINTI') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MasterDataResponseDto>>> saveAll(@RequestBody List<MasterDataNewDto> dataDto) {
        List<MasterDataResponseDto> response = createAllMasterDataUseCase.execute(dataDto);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "Se actualizo el estado", response));
    }

    @PutMapping("/{id}/reparent")
    @PreAuthorize("hasAuthority('ADMINTI') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> reparent(
            @PathVariable Long id,
            @RequestParam Long newParentId
    ) {
        MasterDataResponseDto response = reparentUseCase.execute(id, newParentId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "Nodo reubicado exitosamente", response));
    }

    @GetMapping("/{parentCode}/children")
    public ResponseEntity<ApiResponse<List<MasterDataResponseDto>>> getChildrenByParentCode(
            @PathVariable String parentCode
    ) {
        // 1. Recibe el 'parentCode' de la URL (ej: "PROD_CAT")
        // 2. Lo pasa al caso de uso
        List<MasterDataResponseDto> children = findChildrenUseCase.execute(parentCode);

        // 3. Devuelve la lista de hijos encontrados
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Datos obtenidos exitosamente para el padre: " + parentCode,
                children
        ));
    }
}