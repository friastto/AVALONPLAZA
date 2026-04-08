package org.frias.avalon.domain.constumer;

import org.frias.avalon.domain.constumer.service.ConsumerService;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletWithCatalogProductResponse;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    private final OutletService outletService;
    private final ProductOutletService productOutletService;

    private final ConsumerService consumerService;

    public ConsumerController(OutletService outletService, ProductOutletService productOutletService, ConsumerService consumerService) {
        this.outletService = outletService;
        this.productOutletService = productOutletService;
        this.consumerService = consumerService;
    }


    @PostMapping("/map/nearby/all/stores")
    public List<OutletDto> nearbyAllStores(@RequestBody OutletMap outletDto) {

        return consumerService.nearbyAllOutlets(outletDto);
    }
    @PostMapping("/map/nearby/stores")
    public List<OutletDto> nearbyStores(@RequestBody OutletMap outletDto) {

        return consumerService.nearbyAllOutlets(outletDto);
    }

    @GetMapping("/catalog/store/{id}")
    public OutletWithCatalogProductResponse getOutletsByProduct(@PathVariable Long id) {
        try {
            return productOutletService.getOutletWithCatalogProduct(id);
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("no se pudo hacer esto * "+e.getMessage());
        }
    }

    @PostMapping("/nearby/by-nameproduct")
    public List<OutletsWhitProductMap> getOutletsByProduct(@RequestBody OutletMap product) {
        return productOutletService.getOutletProductByNameProduct(product);
    }

    @GetMapping("/product/{id}")
    public ProductResponseDto getProductOutletById(@PathVariable Long id) {
        return productOutletService.SearchProduct(id);
    }
}
