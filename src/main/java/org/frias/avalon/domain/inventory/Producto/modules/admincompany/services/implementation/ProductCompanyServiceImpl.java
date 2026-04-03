package org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.core.uploadimg.service.ProductUploadImgImpl;
import org.frias.avalon.domain.company.facade.BaseTenantService;
import org.frias.avalon.domain.company.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers.ProductoCompanyMapperService;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeRequestDto;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository.BarcodeCompanyRepository;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository.ProductCompanyRepository;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@TenantAware
@Service
public class ProductCompanyServiceImpl extends BaseTenantService implements ProductoCompanyService {

    private final BarcodeCompanyRepository barcodeCompanyRepository;
    private final ProductoService productService;
    private final CompanyService companyService;
    private final MasterDataService masterDataService;
    private final ProductCompanyRepository productCompanyRepository;
    private final ProductOutletService productOutletService;

    private final ProductoCompanyMapperService productoCompanyMapperService;
    private final ProductUploadImgImpl productUploadImg;

    public ProductCompanyServiceImpl(BarcodeCompanyRepository barcodeCompanyRepository, ProductoService productService, CompanyService companyService, MasterDataService masterDataService, ProductCompanyRepository productCompanyRepository, ProductOutletService productOutletService, ProductoCompanyMapperService productoCompanyMapperService, ProductUploadImgImpl productUploadImg) {
        this.barcodeCompanyRepository = barcodeCompanyRepository;
        this.productService = productService;
        this.companyService = companyService;
        this.masterDataService = masterDataService;

        this.productCompanyRepository = productCompanyRepository;
        this.productOutletService = productOutletService;

        this.productoCompanyMapperService = productoCompanyMapperService;
        this.productUploadImg = productUploadImg;
    }

    @Transactional
    @Override
    public Boolean addBarcode(BarcodeRequestDto barcodeRequestDto) {

        barcodeCompanyRepository.existsByBarcode(
                barcodeRequestDto.newBarcode() // CORRECCIÓN: Validar el código, no la descripción
        ).ifPresent(
                exists -> {throw new EntityExistsException("Este código ya está vinculado a otro producto");}
            );


        Product product = productService.searchById(barcodeRequestDto.productId());

        ProductBarcode newBarcode = ProductBarcode.builder()
                .barcode(barcodeRequestDto.newBarcode())
                .product(product)
                .description(barcodeRequestDto.desc())
                .build();

        ProductBarcode savedBarcode = barcodeCompanyRepository.save(newBarcode);

        // CORRECCIÓN: Retornar el DTO esperado en lugar de null
        return true;

    }

    @Override
    public ProductCompany save(ProductCompany productCompany) {
        // CORRECCIÓN: Implementación básica de guardado
        return productCompanyRepository.save(productCompany);
    }
    @Transactional
    @Override
    public ProductResponseDto addSaasProductToCompanyCatalog(Long idAvalonProduct) {

        Long companyId = getValidatedCompanyId();

        // CORRECCIÓN: Validar nulidad antes de usar el ID
        if (companyId == null) {
            throw new SecurityException("no tiene los permisos necesarios para esta accion");
        }

        Company companyExist = companyService.searchById(companyId);

        Product product = productService.searchById(idAvalonProduct);


        if (productCompanyRepository.existsByProductIdAndCompanyId(product.getId(), companyId)){
            throw new BusinessException("Este producto ya forma parte del catálogo de tu empresa.");
        };

        MasterData statusActive = masterDataService.getStatusActive();

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


        return productoCompanyMapperService.toDto(productCompanySaved);
                /*new ProductResponseDto(
                productCompanySaved.getId(),
                "0",
                productCompanySaved.getCustomName() != null ? productCompanySaved.getCustomName(): productCompanySaved.getProduct().getName(),
                productCompanySaved.getCustomDescription() != null ? productCompanySaved.getCustomDescription(): productCompanySaved.getProduct().getDescription(),
               BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                productCompanySaved.getProduct().getCategory().getFullName(),
                productCompanySaved.getProduct().getUnit().getFullName(),
                "0",
                "no url"

        );*/


    }

    @Override
    public ProductCompany searchProductCompanyByIdProductAvalonProduct(Long idProductAvalon) {

        throw new UnsupportedOperationException("Método no implementado aún");
    }

    @Override
    public List<ProductResponseDto> getAll() {

        List<ProductCompany> catalog = productCompanyRepository.findAll();

        return catalog.stream().map(productoCompanyMapperService::toDto).toList();
    }

    @Override
    public ProductResponseDto update(Long id, ProductRequestCreate request, MultipartFile imgUrl) {

        Long idEmpresa = getValidatedCompanyId();

        ProductCompany productCompany = asignamentAttribute(
                productCompanyRepository.findById(id).orElseThrow(
                        ()-> new EntityNotFoundException("el producto a modificar no esta disponible.")
                ),request);

        String fileName = null;

        if (imgUrl != null && !imgUrl.isEmpty()) {
            fileName = productUploadImg.uploadFile(imgUrl, request.codeBar());

            productCompany.setCustomImageUrl(fileName);
        }


        ProductBarcode barcode = new ProductBarcode();
        barcode.setBarcode(request.codeBar());
        barcode.setCompanyProduct(productCompany);
        barcode.setCompany(new Company(idEmpresa));

        productCompany.addBarcode(barcode);

        return productoCompanyMapperService.toDto(productCompanyRepository.save(productCompany));
    }

    private ProductCompany asignamentAttribute(ProductCompany productCompany, ProductRequestCreate request){


            if(request.name() !=null && !request.name().isBlank()){
                productCompany.setCustomName(request.name());
            }
            if(request.desc() !=null && request.desc().isBlank()){
                productCompany.setCustomDescription(request.desc());

            }
            if(request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0 ){
                productCompany.setCustomPrice(request.price());

            }

        return productCompany;
    }





}
