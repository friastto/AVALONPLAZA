package org.frias.avalon.domain.masterdata.presentation.controllers;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.usecase.changestatus.ChangeStatusUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindMasterDataByIdUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterRoot")
public class MasterRootController {
    private final CreateMasterDataUseCase createUseCase;
    private final FindMasterDataByIdUseCase findByIdUseCase;
    private final ChangeStatusUseCase changeStatusUseCase;
    private final CreateAllMasterDataUseCase createAllMasterDataUseCase;
    private final FindAllMasterDataUseCase findAllUseCase;


    public MasterRootController(CreateMasterDataUseCase createUseCase, FindMasterDataByIdUseCase findByIdUseCase, ChangeStatusUseCase changeStatusUseCase, CreateAllMasterDataUseCase createAllMasterDataUseCase, FindAllMasterDataUseCase findAllUseCase) {
        this.createUseCase = createUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.changeStatusUseCase = changeStatusUseCase;
        this.createAllMasterDataUseCase = createAllMasterDataUseCase;
        this.findAllUseCase = findAllUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> create(@Valid @RequestBody MasterDataNewDto request) {

        Long id = createUseCase.execute(request);
        MasterDataResponseDto response = findByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(201, "se creo el tipo exitosamente", response));
    }

    @GetMapping("/showAll")
    public ResponseEntity<ApiResponse<List<MasterDataResponseDto>>> showAll() {

        List<MasterDataResponseDto> response = findAllUseCase.execute();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se listaron los datos maestros ",
                                response
                        )
                );
    }

    @GetMapping("/search/v2/{id}")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> searchById(@PathVariable Long id) {

        MasterDataResponseDto response = findByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, "", response));
    }


    @PatchMapping("/change/status")
    public ResponseEntity<ApiResponse<MasterDataResponseDto>> updateById(@RequestBody MasterDataUpdateStatusDto dataDto) {

        MasterDataResponseDto response = changeStatusUseCase.execute(dataDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "Se actualiso el estado",
                                response
                        )
                );
    }


    @PostMapping("/save/all")
    public ResponseEntity<ApiResponse<List<MasterDataResponseDto>>> saveAll(@RequestBody List<MasterDataNewDto> dataDto) {

        List<MasterDataResponseDto> response = createAllMasterDataUseCase.execute(dataDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "Se actualizo el estado",
                                response
                        )
                );
    }

}
