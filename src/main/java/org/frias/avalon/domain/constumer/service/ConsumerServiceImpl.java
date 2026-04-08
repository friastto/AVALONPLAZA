package org.frias.avalon.domain.constumer.service;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsumerServiceImpl implements ConsumerService{

    private final OutletService outletService;
    private final ProductOutletService productOutletService;

    public ConsumerServiceImpl(OutletService outletService, ProductOutletService productOutletService) {
        this.outletService = outletService;
        this.productOutletService = productOutletService;
    }


    @Override
    public List<OutletDto> nearbyAllOutlets(OutletMap request) {

        return outletService.searchNearbyStores(request);
    }





}
