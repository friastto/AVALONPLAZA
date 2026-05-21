package org.frias.avalon.domain.person.presentation.controller;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.application.usecase.changestatus.ChangePersonStatusUseCase;
import org.frias.avalon.domain.person.application.usecase.create.CreatePersonUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avalon/person")
public class PersonController {

    private final CreatePersonUseCase createPersonUseCase;
    private final ChangePersonStatusUseCase changeStatusUsecase;

    public PersonController(CreatePersonUseCase createPersonUseCase, ChangePersonStatusUseCase changeStatusUsecase) {
        this.createPersonUseCase = createPersonUseCase;
        this.changeStatusUsecase = changeStatusUsecase;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        PersonResponse personResponse = createPersonUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Persona creada exitosamente",
                        personResponse
                ));
    }

    @PostMapping("/{idPerson}/change/statusTo/{idStatus}")
    public ResponseEntity<ApiResponse<PersonResponse>> changeStatus(@PathVariable Long idPerson, @PathVariable Long idStatus) {

        PersonResponse personResponse = changeStatusUsecase.execute(idPerson, idStatus);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Persona creada exitosamente",
                        personResponse
                ));
    }
}