package org.frias.avalon.domain.product.presentation.controller.company;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.usecase.inter.company.AssingProductToCompanyUseCase;
import org.frias.avalon.domain.product.application.usecase.inter.saas.GetAllProductsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avalon/admin/company/product")
public class ProductCompanyController {


    private final GetAllProductsUseCase getAllProductsUseCase;
private final AssingProductToCompanyUseCase assingProductToCompanyUseCase;

    public ProductCompanyController(GetAllProductsUseCase getAllProductsUseCase, AssingProductToCompanyUseCase assingProductToCompanyUseCase) {
        this.getAllProductsUseCase = getAllProductsUseCase;
        this.assingProductToCompanyUseCase = assingProductToCompanyUseCase;
    }


    @GetMapping("/all")
    public ResponseEntity getAllProductAvalon(){

        List<ProductAvalonResponseDto> productAvalonList =  getAllProductsUseCase.execute();
        String message =  "extraccion de Productos Avalon exitoso";

        if(productAvalonList.isEmpty()) message = "no se han registrado productos en avalon";

       return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(
               200,
               message,
               productAvalonList
               ));
    }
    @PostMapping("/assing/{id}")
    public ResponseEntity asingTo(@PathVariable Long id){

        ProductResponseDto productCompanyAssing =  assingProductToCompanyUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(
                200,
                "producto añadido co exito",
                productCompanyAssing
        ));
    }
}
