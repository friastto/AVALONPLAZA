package org.frias.avalon.Producto.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;
import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.Producto.mappers.ProductoMapperService;
import org.frias.avalon.Producto.repositories.ProductRepository;
import org.frias.avalon.Producto.services.interfaces.ProductoService;
import org.frias.avalon.Producto.services.interfaces.ProductoServiceEcommerce;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.repositories.MasterDataRepository;
import org.frias.avalon.maestra.services.interfaces.MasterDataProductService;
import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.frias.avalon.promociones.factory.oters.PromotionFactoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductoService
        , ProductoServiceEcommerce {

    private final PromotionFactoryService promoFactoryService;
    private final ProductRepository productRepository;
    private final ConvertFactoryService convertFactoryService;
    private final MasterDataProductService masterDataService;

    private final String status = "ACT";


    private final Set<String> unitMasaPesable = Set.of("KG","LB","GR");
    private final ProductoMapperService productoMapperService;

    public ProductServiceImpl(PromotionFactoryService promoFactoryService, ProductRepository productRepository, ConvertFactoryService convertFactoryService, MasterDataRepository masterDataRepository, MasterDataProductService masterDataService, ProductoMapperService productoMapperService) {
        this.promoFactoryService = promoFactoryService;
        this.productRepository = productRepository;
        this.convertFactoryService = convertFactoryService;
        this.masterDataService = masterDataService;
        this.productoMapperService = productoMapperService;
    }



    @Override
    public BigDecimal calculatePrice(Long productId, String identity) {



        return null;
    }

    @Transactional
    @Override
    public ProductResponseDto save(ProductRequestCreate request) {

        boolean isDecimal = request.stock().contains(".") || request.stock().contains(",");

        String cleanPrice = isDecimal ?
                request.stock().replace(",", ".")
                : request.stock();

        if( productRepository.findBySku(request.codeBar()).isPresent())
            throw new EntityExistsException("!codigo de barras no disponible¡");


        MasterData unit = masterDataService.findById(request.unitId());

        if (!unitMasaPesable.contains(unit.getShortName()) && isDecimal)
            throw new IllegalArgumentException("la unidad de medida :"
                    + unit.getShortName()
                    +" solo acepta numeros enteros ");


        MasterData category = masterDataService.findById(request.categoryId());

        MasterData status = masterDataService.findClientByNameShort("ACT");

        Product productNewEntity = new Product();

        productNewEntity.setSku(request.codeBar());
        productNewEntity.setName(request.name());
        productNewEntity.setDescription(request.desc());
        productNewEntity.setPrice(request.price());
        productNewEntity.setCategory(category);
        productNewEntity.setUnit(unit);

        BigDecimal stockNewEntry = convertFactoryService.convertTo(cleanPrice, unit.getShortName(),false);

        productNewEntity.setStock(stockNewEntry.intValue());

        productNewEntity.setStatus(status);

         Product productSaved = productRepository.save(productNewEntity);

        return createDto(productSaved);

    }

    @Override
    public ProductResponseDto findByCodeBar(String codeBar) {



        return createDto(productRepository.findBySku(codeBar)
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


    private ProductResponseDto createDto(Product productSaved) {

        DiscountTempResult resultDiscount = promoFactoryService.getFinalPrice(productSaved, false);

//
//        Promotion promotionActive = Optional.ofNullable(productSaved.getPromotions()) // Si es null, crea un Optional vacío
//                .orElse(Collections.emptyList()) // Si era null, usa una lista vacía
//                .stream()
//                .filter(Promotion::estaActiva)
//                .findFirst()
//                .orElse(null);

        return new ProductResponseDto(
                productSaved.getId()
                , productSaved.getSku()
                , productSaved.getName()
                , productSaved.getDescription()
                , productSaved.getPrice()
                , resultDiscount.discount()
                , resultDiscount.priceFinal()
                , productSaved.getCategory().getFullName()
                , productSaved.getUnit().getFullName()

                , convertFactoryService.convertTo(
                productSaved.getStock().toString()
                , productSaved.getUnit().getShortName()
                , true).toString()
        );
    }

    @Override
    public ProductResponseDto findById(Long id) {

        return createDto(productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("producto no disponible")));
    }

    @Override
    public Product searchById(Long id) {

        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("producto no disponible"));
    }
}
