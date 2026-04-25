package org.frias.avalon.domain.masterdata.presentation.controllers;


import jakarta.validation.Valid;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masterData")
public class MasterDataController {


   private final  MasterDataService mdservice;

    public MasterDataController(MasterDataService mdservice) {
        this.mdservice = mdservice;
    }


    @PostMapping("/saveAll")
    public ResponseEntity<List<MasterData>> saveAll(@Valid  @RequestBody List<MasterDataNewDto> mdrequest) {

        System.out.println("pk1 : "+ mdrequest.toString()


        );

            return ResponseEntity.ok(mdservice.createAll(mdrequest));

    }

    @GetMapping("/search/v1/{shortName}")
    public MasterData searchByNameShort(@PathVariable String shortName) {

        return mdservice.searchByShortName(shortName);
    }


    @GetMapping("/search/v2")
    public List<MasterData> getByAllWithStatusActive() {

        return mdservice.getAllWithStatusActive();
    }

}
