package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.implementation;

import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.mapper.ProductOutletMapperService;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.repository.ProductoOutletRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseMap;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers.ProductoMapperService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@TenantAware
@Service
public class ProductOutletServiceImpl implements ProductOutletService {

    private final ProductoOutletRepository productoOutletRepository;
    private final ProductoMapperService productoMapperService;
    private final ProductOutletMapperService productOutletMapperService;

    public ProductOutletServiceImpl(ProductoOutletRepository productoOutletRepository, ProductoMapperService productoMapperService, ProductOutletMapperService productOutletMapperService) {
        this.productoOutletRepository = productoOutletRepository;
        this.productoMapperService = productoMapperService;

        this.productOutletMapperService = productOutletMapperService;
    }


    @Override
    public List<ProductOutletResponseDto> getAllProductCatalog() {

        Long outLetId = TenantContext.getTenantOutletId();

        return getProductCatalogToOutlet(outLetId);
    }

    @Override
    public List<ProductOutletResponseDto> getProductCatalogToOutlet(Long id) {

        List<ProductOutlet> productOutlets = productoOutletRepository.findAllByOutletIdWithHierarchy(
                id
        );

        return productOutlets.stream()
                .map(productOutletMapperService::toDto)
                .toList();
    }

    @Override
    public List<OutletsWhitProductMap> getOutletProductByNameProduct(OutletMap requestMap) {

        List<ProductOutlet> pots = productoOutletRepository.findByNameAndOutletRadius(
                requestMap.query(),
                requestMap.lat(),
                requestMap.lng(),
                requestMap.radius()
        );
        // Si no hay nada, cortamos camino de una vez
        if (pots.isEmpty()) {
            return Collections.emptyList(); // Devuelve []
        }


        // 2. Agrupamos por Outlet y transformamos a tus records específicos
        return pots.stream()
                .collect(Collectors.groupingBy(ProductOutlet::getOutlet))
                .entrySet().stream()
                .map(entry -> {
                    // 'entry.getKey()' es el objeto Outlet
                    Outlet outlet = entry.getKey();

                    // Transformamos la lista de ProductOutlet a tu record ProductResponseMap
                    List<ProductResponseMap> productList = entry.getValue().stream()
                            .map(po -> new ProductResponseMap(
                                    po.getId(),
                                    po.getCustomName() != null ? po.getCustomName() : po.getCompanyProduct().getCustomName(),
                                    String.valueOf(po.getStock()) // Convertimos stock a String como pide tu record
                            ))
                            .toList();

                    // Retornamos tu record principal
                    return new OutletsWhitProductMap(
                            outlet.getId(),
                            outlet.getName(),
                            outlet.getLatitude(),
                            outlet.getLongitude(),
                            productList
                    );
                })
                .toList();
    }

    @Override
    public void addAll(List<ProductOutlet> productOutletList) {
        productoOutletRepository.saveAll(productOutletList);
    }
}
