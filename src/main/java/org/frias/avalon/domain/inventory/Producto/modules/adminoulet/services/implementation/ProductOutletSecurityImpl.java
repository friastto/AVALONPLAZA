package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.mapper.ProductOutletMapperService;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.repository.ProductOutletRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.ProductResponseMap;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.mapper.ProductoOutletMapperService;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletWithCatalogProductResponse;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@TenantAware
@Service
public class ProductOutletSecurityImpl extends TenantSecurity implements ProductOutletService {

    private final ProductOutletRepository productOutletRepository;
    private final ProductoMapperService productoMapperService;
    private final ProductOutletMapperService productOutletMapperService;
    private final ProductoOutletMapperService productoOutletMapperService;
    private final OutletService outletService;

    public ProductOutletSecurityImpl(ProductOutletRepository productOutletRepository, ProductoMapperService productoMapperService, ProductOutletMapperService productOutletMapperService, ProductoOutletMapperService productoOutletMapperService, OutletService outletService) {
        this.productOutletRepository = productOutletRepository;
        this.productoMapperService = productoMapperService;

        this.productOutletMapperService = productOutletMapperService;
        this.productoOutletMapperService = productoOutletMapperService;
        this.outletService = outletService;
    }


    @Override
    public List<ProductOutletResponseDto> getAllProductCatalog() {

        Long outLetId = getValidatedOutletId();

        return getProductCatalogToOutlet(outLetId);
    }

    @Override
    public List<ProductOutletResponseDto> getProductCatalogToOutlet(Long id) {

        List<ProductOutlet> productOutlets = productOutletRepository.findAllByOutletIdWithHierarchy(id);

        return productOutlets.stream()
                .map(productOutletMapperService::toDto)
                .toList();
    }

    @Override
    public List<OutletsWhitProductMap> getOutletProductByNameProduct(OutletMap requestMap) {

        List<ProductOutlet> pots = productOutletRepository.findByNameAndOutletRadius(
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
                                    po.getLocalName() != null ? po.getLocalName() : po.getCompanyProduct().getCustomName(),
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
        productOutletRepository.saveAll(productOutletList);
    }

    @Override
    public ProductResponseDto SearchProduct(Long id) {

        ProductOutlet po = productOutletRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("no se encuentra registrado el producto o no esta habilitado para la venta"));


        return productoOutletMapperService.toDto(po);
    }

    @Override
    public OutletWithCatalogProductResponse getOutletWithCatalogProduct(Long id) {

        Outlet o = outletService.searchById(id);

        List<ProductOutlet> productOutlets = productOutletRepository.findAllByOutletIdWithHierarchy(id);


        // Agrupamos por el name de la categoría y convertimos a DTO
        Map<String, List<ProductResponseDto>> catalogDto = productOutlets.stream()
                .map(productoOutletMapperService::toDto) // Referencia directa al mapper
                .collect(Collectors.groupingBy(ProductResponseDto::category)); // Agrupa por el campo categoría del DTO

        return new OutletWithCatalogProductResponse(
                o.getId(),
                o.getName(),
                catalogDto
        );
    }
}
