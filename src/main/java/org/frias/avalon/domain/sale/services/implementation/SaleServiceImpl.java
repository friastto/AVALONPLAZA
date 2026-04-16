package org.frias.avalon.domain.sale.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.InsufficientStockException;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.repository.ProductOutletRepository;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataSalesService;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.repository.PersonaRepository;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.domain.promotion.fabric.convertermasa.factory.ConvertFactoryService;
import org.frias.avalon.domain.promotion.fabric.discountpath.DiscountPathRoleFactory;
import org.frias.avalon.domain.sale.dtos.SaleDetailRequest;
import org.frias.avalon.domain.sale.dtos.SaleRequest;
import org.frias.avalon.domain.sale.dtos.SalesResponseDto;
import org.frias.avalon.domain.sale.entities.Sale;
import org.frias.avalon.domain.sale.entities.SaleDetail;
import org.frias.avalon.domain.sale.mappers.SalesMapperService;
import org.frias.avalon.domain.sale.repositories.SaleRepository;
import org.frias.avalon.domain.sale.services.interfaces.SaleService;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.infraestruture.repositories.UserRepository;
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
    private final SaleRepository saleRepository;
    private final ProductOutletRepository productOutletRepository;

    private final UserRepository userRepository;

    private final PersonService personaService;

    private final MasterDataSalesService masterDataSalesService;
    private final ConvertFactoryService convertFactoryService;

    private  final DiscountPathRoleFactory priceCalculator;

    private final Set<String> unitMasaPesable = Set.of("KG","LB","GR");

    private final SalesMapperService salesMapperService;

    public SaleServiceImpl(ProductoService productoService, SaleRepository saleRepository, ProductOutletRepository productOutletRepository, UserRepository userRepository, PersonaRepository personaRepository, PersonService personaService, MasterDataSalesService masterDataSalesService, ConvertFactoryService convertFactoryService, DiscountPathRoleFactory priceCalculator, SalesMapperService salesMapperService) {
        this.productoService = productoService;
        this.saleRepository = saleRepository;
        this.productOutletRepository = productOutletRepository;
        this.userRepository = userRepository;
        this.personaService = personaService;
        this.masterDataSalesService = masterDataSalesService;

        this.convertFactoryService = convertFactoryService;

        this.priceCalculator = priceCalculator;
        this.salesMapperService = salesMapperService;
    }



    @Transactional
    @Override
    public SalesResponseDto salesProccesor(SaleRequest saleRequest) {

        Person customer;

        try{
            customer = personaService.findByNumberId(saleRequest.customerId());


        }catch(EntityNotFoundException ex){
            String CUSTOMER_GENERIC_ID = "999";
            customer = personaService.findByNumberId(CUSTOMER_GENERIC_ID);
        }


        UserAvalon employee = userRepository.findById(saleRequest.enployeeId()).orElseThrow();

        MasterData metodPay = masterDataSalesService.searchById(saleRequest.metodoPagoId());

        MasterData status = masterDataSalesService.searchByShortName("COM");

        Sale saleEntity = new Sale();

        saleEntity.setCustomerId(customer);

        saleEntity.setEnployeeId(employee);

        saleEntity.setPaymentMethodId(metodPay);

        saleEntity.setAmountReceived(saleRequest.amountReceived());

        saleEntity.setSaleDateAt(LocalDateTime.now());

        List<String> roles = userRepository.findRolesByPersonNumberId(saleRequest.customerId());

       // String descripcionDetalils;
       List<SaleDetail> details = new ArrayList<>();

        for (SaleDetailRequest sd : saleRequest.saleDetails()){

           String quantityCleaned = sd.quantity().contains(",")||sd.quantity().contains(".")
            ? sd.quantity().replace(",",".")
                   : sd.quantity();

            ProductOutlet productOutlet = productOutletRepository.findByProductIdAndOutletId(
                    sd.productId(), 1L).orElseThrow(() -> new EntityNotFoundException("Product not found")
            );


                     Product productEntity = productOutlet.getCompanyProduct().getProduct();

            Integer cantRequired;

            if (!unitMasaPesable.contains(productEntity.getUnit().getShortName())
                    && (sd.quantity().contains(".")||sd.quantity().contains(","))
            )
                throw new IllegalArgumentException(
                        "la cantidad ingresada no corresponde a la unidad de unitMeasure del producto : "
                                + productEntity.getUnit().getShortName()
                );

            if(unitMasaPesable.contains(productEntity.getUnit().getShortName())) {

                cantRequired = convertFactoryService.convertTo(
                        quantityCleaned
                        , productEntity.getUnit().getShortName()
                        , false
                ).intValue();
            }else {
                cantRequired = Integer.valueOf(sd.quantity());
            }

          if (productOutlet.getStock() < cantRequired)
              throw new InsufficientStockException(
                      "Stock insuficiente *code :" + productEntity.getBarcodes().get(1)+ " *name: " + productEntity.getName()
              );

            SaleDetail sdEntity = new SaleDetail();

            sdEntity.setProduct(productEntity);


            DiscountTempResult precioUnitario = priceCalculator.calculate(productOutlet,roles, "1");

            DiscountTempResult subTotal = priceCalculator.calculate(productOutlet,roles, quantityCleaned);

            System.out.println("\n****************************\n" +
                    subTotal.description()
                    +
            "\n****************************\n");
            sdEntity.setQuantity(cantRequired);

            sdEntity.setUnitPrice(precioUnitario.priceFinal());

            //System.out.println(precioUnitario.priceFinal());

            sdEntity.setSubTotal(subTotal.priceFinal());

            sdEntity.setSale(saleEntity);
            sdEntity.setProduct(productEntity);

            System.out.println("se redujo el stock de "+productEntity.getName() + " cantActual ( " + productOutlet.getStock()+" - "+cantRequired +" = "+(productOutlet.getStock()-cantRequired)+" ) \n");

            productOutlet.setStock(productOutlet.getStock() - cantRequired);

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

        if (saleRequest.amountReceived().compareTo(valueTotalSale) < 0) {
            throw new RuntimeException("El monto recibido ($" + saleRequest.amountReceived() +
                    ") es insuficiente para cubrir el total ($ " + valueTotalSale + ")");
        }

        return salesMapperService.toResponseDto(saleRepository.save(saleEntity));
    }


}
