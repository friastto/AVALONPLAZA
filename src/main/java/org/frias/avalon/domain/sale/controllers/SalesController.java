package org.frias.avalon.domain.sale.controllers;


import org.frias.avalon.domain.sale.dtos.SaleRequest;
import org.frias.avalon.domain.sale.dtos.SalesResponseDto;
import org.frias.avalon.domain.sale.services.interfaces.SaleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sale")
public class SalesController {

    private final SaleService saleService;


    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping("/save")
    public SalesResponseDto saveSale(@RequestBody SaleRequest saleRequest) {

            return saleService.salesProccesor(saleRequest);
    }


}
