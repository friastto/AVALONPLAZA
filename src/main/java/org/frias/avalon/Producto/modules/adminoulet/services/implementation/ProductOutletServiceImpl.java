package org.frias.avalon.Producto.modules.adminoulet.services.implementation;

import org.frias.avalon.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.Producto.modules.adminoulet.mapper.ProductOutletMapperService;
import org.frias.avalon.Producto.modules.adminoulet.repository.ProductoOutletRepository;
import org.frias.avalon.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.Producto.modules.adminsaas.mappers.ProductoMapperService;
import org.frias.avalon.empresasucursal.tenant.config.TenantAware;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
