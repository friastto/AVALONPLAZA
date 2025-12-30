package org.frias.avalon.ventas.services.implementation;

import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.Producto.services.interfaces.ProductoService;
import org.frias.avalon.exeptions.InsufficientStockException;
import org.frias.avalon.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.fabric.discountpath.DiscountPathRoleFactory;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.services.interfaces.MasterDataSalesService;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.person.repository.PersonaRepository;
import org.frias.avalon.person.services.interfaces.PersonService;
import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.frias.avalon.promociones.factory.oters.PromotionFactoryService;
import org.frias.avalon.user.entities.User;
import org.frias.avalon.user.repositories.UsuarioRepository;
import org.frias.avalon.ventas.dtos.SaleDetailRequest;
import org.frias.avalon.ventas.dtos.SaleRequest;
import org.frias.avalon.ventas.dtos.SalesResponseDto;
import org.frias.avalon.ventas.entities.Sale;
import org.frias.avalon.ventas.entities.SaleDetail;
import org.frias.avalon.ventas.mappers.SalesMapperService;
import org.frias.avalon.ventas.repositories.SaleRepository;
import org.frias.avalon.ventas.services.interfaces.SaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SaleServiceImpl implements SaleService {

    private final ProductoService productoService;
    private final PromotionFactoryService promotionFactoryService;
    private final SaleRepository saleRepository;

    private final UsuarioRepository usuarioRepository;

    private final PersonService personaService;

    private final MasterDataSalesService masterDataSalesService;
    private final ConvertFactoryService convertFactoryService;

    private  final DiscountPathRoleFactory priceCalculator;

    private final Set<String> unitMasaPesable = Set.of("KG","LB","GR");

    private final SalesMapperService salesMapperService;
    public SaleServiceImpl(ProductoService productoService, PromotionFactoryService promotionFactoryService, SaleRepository saleRepository, UsuarioRepository usuarioRepository, PersonaRepository personaRepository, PersonService personaService, MasterDataSalesService masterDataSalesService, ConvertFactoryService convertFactoryService, DiscountPathRoleFactory priceCalculator, SalesMapperService salesMapperService) {
        this.productoService = productoService;
        this.promotionFactoryService = promotionFactoryService;
        this.saleRepository = saleRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaService = personaService;
        this.masterDataSalesService = masterDataSalesService;

        this.convertFactoryService = convertFactoryService;

        this.priceCalculator = priceCalculator;
        this.salesMapperService = salesMapperService;
    }



    @Transactional
    @Override
    public SalesResponseDto salesProccesor(SaleRequest saleRequest) {

        Person customer = personaService.findByNumberId(saleRequest.customerId());

        User employee = usuarioRepository.findById(saleRequest.enployeeId()).orElseThrow();

        MasterData metodPay = masterDataSalesService.findById(saleRequest.metodoPagoId());


        MasterData status = masterDataSalesService.searchShortName("COM");



        Sale saleEntity = new Sale();

        saleEntity.setCustomerId(customer);
        saleEntity.setEnployeeId(employee);
        saleEntity.setPaymentMethodId(metodPay);


        saleEntity.setAmountReceived(saleRequest.amountReceived());
        saleEntity.setSaleDateAt(LocalDateTime.now());

        List<String> roles = usuarioRepository.findRolesByPersonNumberId(saleRequest.customerId());

       List<SaleDetail> details = new ArrayList<>();
        for (SaleDetailRequest sd : saleRequest.saleDetails()){

           String quantityCleaned = sd.quantity().contains(",")||sd.quantity().contains(".")
            ? sd.quantity().replace(",",".")
                   : sd.quantity();

            Product productEntity = productoService.searchById(sd.productId());

            Integer cantRequired;

            if (!unitMasaPesable.contains(productEntity.getUnit().getShortName()) && (sd.quantity().contains(".")||sd.quantity().contains(",")))
                throw new IllegalArgumentException("la cantidad ingresada no corresponde a la unidad de medida del producto : "+productEntity.getUnit().getShortName());

            if(unitMasaPesable.contains(productEntity.getUnit().getShortName())) {
                cantRequired = convertFactoryService.convertTo(quantityCleaned, productEntity.getUnit().getShortName(), false).intValue();
            }else {
                cantRequired = Integer.valueOf(sd.quantity());
            }

          if (productEntity.getStock() < cantRequired)
              throw new InsufficientStockException(
                      "Stock insuficiente *code :" + productEntity.getSku()+ " *name: " + productEntity.getName()
              );

            SaleDetail sdEntity = new SaleDetail();

            sdEntity.setProduct(productEntity);

            DiscountTempResult subTotal = priceCalculator.calculate(productEntity,roles, quantityCleaned);
            DiscountTempResult precioUnitario = priceCalculator.calculate(productEntity,roles, "1");

            sdEntity.setQuantity(cantRequired);

            sdEntity.setUnitPrice(precioUnitario.priceFinal());

            //System.out.println(precioUnitario.priceFinal());

            sdEntity.setSubTotal(subTotal.priceFinal());

            sdEntity.setSale(saleEntity);
            sdEntity.setProduct(productEntity);

            System.out.println("se redujo el stock de "+productEntity.getName() + " cantActual ( " + productEntity.getStock()+" - "+cantRequired +" = "+(productEntity.getStock()-cantRequired)+" )");

            productEntity.setStock(productEntity.getStock() - cantRequired);

            details.add(sdEntity);

        };

        saleEntity.setDetails(details);

        BigDecimal valueTotalSale = saleEntity.getDetails().stream()
                .map(SaleDetail::getSubTotal)
                .filter(Objects::nonNull) // Seguridad por si algún subtotal es null
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        saleEntity.setTotal(valueTotalSale);

        saleEntity.setAmountReturned(saleEntity.getTotal().subtract(saleEntity.getAmountReceived()));
        saleEntity.setStatusId(status);

        return salesMapperService.toResponseDto(saleRepository.save(saleEntity));
    }


}
