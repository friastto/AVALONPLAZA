package org.frias.avalon.maestra.controllers;


import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/masterData")
public class MasterDataController {


   private final  MasterDataService mdservice;

    public MasterDataController(MasterDataService mdservice) {
        this.mdservice = mdservice;
    }


    @PostMapping("/saveAll")
    public ResponseEntity<List<MasterDataResponseDto>> saveAll(@RequestBody List<MasterDataRequestCreateDto> mdrequest) {

            return ResponseEntity.ok(mdservice.saveAll(mdrequest));

    }

    @PostMapping("/search/v1")
    public MasterDataResponseDto saveAll(@RequestParam String shortName) {
        return mdservice.findByNameShort(shortName);
    }
}
