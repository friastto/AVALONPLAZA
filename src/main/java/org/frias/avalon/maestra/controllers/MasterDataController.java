package org.frias.avalon.maestra.controllers;


import jakarta.validation.Valid;
import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
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
    public ResponseEntity<List<MasterDataResponseDto>> saveAll(@Valid  @RequestBody List<MasterDataRequestCreateDto> mdrequest) {

        System.out.println("pk1 : "+ mdrequest.toString()


        );

            return ResponseEntity.ok(mdservice.saveAll(mdrequest));

    }

    @PostMapping("/search/v1")
    public MasterDataResponseDto saveAll(@RequestParam String shortName) {
        return mdservice.findByNameShortDto(shortName);
    }
}
