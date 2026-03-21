package org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.implementation;

import jakarta.persistence.EntityExistsException;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeRequestDto;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeResponseNewDto;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository.ProductCompanyRepository;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository.BarcodeCompanyRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.exeptions.BusinessException;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductCompanyServiceImpl implements ProductoCompanyService {

    private final BarcodeCompanyRepository barcodeCompanyRepository;
    private final ProductoService productService;
    private final CompanyService companyService;
    private final MasterDataService masterDataService;
    private final ProductCompanyRepository productCompanyRepository;
    private final ProductOutletService productOutletService;


    public ProductCompanyServiceImpl(BarcodeCompanyRepository barcodeCompanyRepository, ProductoService productService, CompanyService companyService, MasterDataService masterDataService, ProductCompanyRepository productCompanyRepository, ProductOutletService productOutletService) {
        this.barcodeCompanyRepository = barcodeCompanyRepository;
        this.productService = productService;
        this.companyService = companyService;
        this.masterDataService = masterDataService;

        this.productCompanyRepository = productCompanyRepository;
        this.productOutletService = productOutletService;

    }

    @Transactional
    @Override
    public BarcodeResponseNewDto addBarcode(BarcodeRequestDto barcodeRequestDto) {

        barcodeCompanyRepository.existsByBarcode(
                barcodeRequestDto.desc()
        ).ifPresent(
                exists -> {throw new EntityExistsException("Este código ya está vinculado a otro producto");}
            );


        Product product = productService.searchById(barcodeRequestDto.productId());

        ProductBarcode newBarcode = ProductBarcode.builder()
                .barcode(barcodeRequestDto.newBarcode())
                .product(product)
                .description(barcodeRequestDto.desc())
                .build();

        barcodeCompanyRepository.save(newBarcode);

        return null;
    }

    @Override
    public ProductCompany save(ProductCompany productCompany) {


        return null;
    }
    @Transactional
    @Override
    public ProductResponseDto addSaasProductToCompanyCatalog(Long idAvalonProduct) {


        Long companyId = TenantContext.getTenantId();

        Company companyExist = companyService.findById(companyId);

        if (companyId == null) {
            throw new SecurityException("no tiene los permisos necesarios para esta accion");
        }

        Product product = productService.searchById(idAvalonProduct);


        if (productCompanyRepository.existsByProductIdAndCompanyId(product.getId(), companyId)){
            throw new BusinessException("Este producto ya forma parte del catálogo de tu empresa.");
        };

        MasterData statusActive = masterDataService.searchShortName("ACT");

        ProductCompany productCompany = new ProductCompany();
        productCompany.setProduct(product);
        productCompany.setCompany(companyExist);
        productCompany.setStatus(statusActive);

        ProductCompany productCompanySaved = productCompanyRepository.save(productCompany);

        List<Outlet> outlets = companyExist.getOutlets();

        // 3. Crear el "cascarón" en cada sucursal
        List<ProductOutlet> initialInventory = outlets.stream().map(outlet ->
                ProductOutlet.builder()
                        .companyProduct(productCompanySaved)
                        .outlet(outlet)
                        .company(companyExist)
                        .stock(0) // Empieza en cero
                        .localPrice(BigDecimal.ZERO) // El admin lo cambiará luego
                        .active(true)
                        .build()
        ).toList();

        productOutletService.addAll(initialInventory);

        return new ProductResponseDto(
                productCompanySaved.getId(),
                null,
                productCompanySaved.getCustomName() != null ? productCompanySaved.getCustomName(): productCompanySaved.getProduct().getName(),
                productCompanySaved.getCustomDescription() != null ? productCompanySaved.getCustomDescription(): productCompanySaved.getProduct().getDescription(),
                null,
                null,
                null,
                productCompanySaved.getProduct().getCategory().getFullName(),
                productCompanySaved.getProduct().getUnit().getFullName(),
                null,
                null

        );



    }

    @Override
    public ProductCompany searchProductCompanyByIdProductAvalonProduct(Long idProductAvalon) {
        return null;
    }
}
