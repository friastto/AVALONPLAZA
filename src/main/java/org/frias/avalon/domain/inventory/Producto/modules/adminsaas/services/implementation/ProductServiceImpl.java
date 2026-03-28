package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.repositories.MasterDataRepository;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataProductService;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.frias.avalon.domain.promotion.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductBarcodeRequestDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers.ProductoMapperService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.repositories.BarcodeRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.repositories.ProductRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoBarcodeServices;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoServiceEcommerce;
import org.frias.avalon.core.uploadimg.removebg.RemoveBgService;
import org.frias.avalon.core.uploadimg.service.ProductUploadImgImpl;
import org.frias.avalon.domain.inventory.promo.factory.oters.PromotionFactoryService;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@TenantAware
@Service
public class ProductServiceImpl implements ProductoService
        , ProductoServiceEcommerce
, ProductoBarcodeServices {

    private final PromotionFactoryService promoFactoryService;
    private final ProductRepository productRepository;
    private final BarcodeRepository barcodeRepository;
    private final ConvertFactoryService convertFactoryService;
    private final MasterDataProductService masterDataService;
    private final ProductUploadImgImpl productUploadImg;
    private final RemoveBgService removeBgService;

    private final String status = "ACT";


    private final Set<String> unitMasaPesable = Set.of("KG","LB","GR");
    private final ProductoMapperService productoMapperService;
    private final JwtUtils jwtUtils;

    public ProductServiceImpl(PromotionFactoryService promoFactoryService, ProductRepository productRepository, BarcodeRepository barcodeRepository, ConvertFactoryService convertFactoryService, MasterDataRepository masterDataRepository, MasterDataProductService masterDataService, ProductUploadImgImpl productUploadImg, RemoveBgService removeBgService, ProductoMapperService productoMapperService, JwtUtils jwtUtils) {
        this.promoFactoryService = promoFactoryService;
        this.productRepository = productRepository;
        this.barcodeRepository = barcodeRepository;
        this.convertFactoryService = convertFactoryService;
        this.masterDataService = masterDataService;
        this.productUploadImg = productUploadImg;
        this.removeBgService = removeBgService;
        this.productoMapperService = productoMapperService;
        this.jwtUtils = jwtUtils;
    }



    @Override
    public BigDecimal calculatePrice(Long productId, String identity) {



        return null;
    }

    @Transactional
    @Override
    public ProductResponseDto save(ProductRequestCreate request, MultipartFile imgUrl) {

        boolean isDecimal = request.stock().contains(".") || request.stock().contains(",");

        String cleanPrice = isDecimal ?
                request.stock().replace(",", ".")
                : request.stock();

        if( barcodeRepository.existsByBarcode(request.codeBar()))
            throw new EntityExistsException("!codigo de barras no disponible¡");


        MasterData unit = masterDataService.searchById(request.unitId());

        if (!unitMasaPesable.contains(unit.getShortName()) && isDecimal)
            throw new IllegalArgumentException("la unidad de medida :"
                    + unit.getShortName()
                    +" solo acepta numeros enteros ");

        // 3. Obtener contexto (Quién está creando esto)
        Long currentCompanyId = TenantContext.getTenantId();
        // Supongamos que pasas el outletId en el request


        MasterData category = masterDataService.searchById(request.categoryId());

        MasterData status = masterDataService.searchByNameShortAndStatusActive("ACT");


        Product productNewEntity = new Product();

        //productNewEntity.setSku(request.codeBar());
        productNewEntity.setName(request.name());
        productNewEntity.setDescription(request.desc());
        //productNewEntity.setPrice(request.price());
        productNewEntity.setCategory(category);
        productNewEntity.setUnit(unit);


        BigDecimal stockNewEntry = convertFactoryService.convertTo(cleanPrice, unit.getShortName(),false);

        //productNewEntity.setStock(stockNewEntry.intValue());

        productNewEntity.setStatus(status);


        String fileName = productUploadImg.uploadFile(imgUrl,request.codeBar());

        productNewEntity.setImageUrl(fileName);

         Product productSaved = productRepository.save(productNewEntity);

        ProductBarcode pbc = new ProductBarcode();
        pbc.setBarcode(request.codeBar());
        pbc.setProduct(productSaved);


        barcodeRepository.save(pbc);

        return null;//createDto(productSaved);

    }

    @Override
    public ProductResponseDto findByCodeBar(String codeBar) {



        return null ;/*createDto(productRepository.findBySku(codeBar)
                .orElseThrow(() -> new EntityNotFoundException("producto :" +codeBar+" no disponible"))
        );

    }



    @Override
    public ProductResponseDto searchByName(String name) {



        return null;
    }

    @Override
    public List<ProductResponseDto> findAll() {

        List<Product> products = productRepository.findAll();
       if( products.isEmpty()) throw new EntityNotFoundException("no se encontraron registros");

        return products.stream()
                    .map(p ->{
                        return createDto(p);
                    }).collect(Collectors.toList())
                ;
    }


    private ProductResponseDto createDto(ProductOutlet productSaved) {

        DiscountTempResult resultDiscount = promoFactoryService.getFinalPrice(productSaved, false);

        String productImageUrl = productUploadImg.getPresignedUrl(productSaved.getLocalImageUrl());

//        Promotion promotionActive = Optional.ofNullable(productSaved.getPromotions()) // Si es null, crea un Optional vacío
//                .orElse(Collections.emptyList()) // Si era null, usa una lista vacía
//                .stream()
//                .filter(Promotion::estaActiva)
//                .findFirst()
//                .orElse(null);

        return null ;/* new ProductResponseDto(

                productSaved.getId()
                //, productSaved.getSku()
                , productSaved.getName()
                , productSaved.getDescription()
                //, productSaved.getPrice()
                , resultDiscount.discount()
                , resultDiscount.priceFinal()
                , productSaved.getCategory().getFullName()
                , productSaved.getUnit().getFullName()

                /*, convertFactoryService.convertTo(
                    productSaved.getStock().toString()
                    , productSaved.getUnit().getShortName()
                    , true).toString()



                , productImageUrl

        );*/
    }

    @Override
    public ProductResponseDto searchByName(String name) {
        return null;
    }

    @Override
    public List<ProductResponseDto> findAll() {
            List<Product> p = productRepository.findAll();

        return p.stream()
                .map(productoMapperService::toDto)
                .toList();
    }

    @Override
    public ProductResponseDto findById(Long id) {

        return null;//createDto(productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("producto no disponible")));
    }

    @Override
    public Product searchById(Long id) {

        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("producto no disponible"));
    }

    @Override
    public boolean addBarcode(ProductBarcodeRequestDto productBarcodeRequestDto) {

        barcodeRepository.existsByBarcode(productBarcodeRequestDto.newBarcode());

        return true;
    }
}
