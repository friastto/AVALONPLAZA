package org.frias.avalon.domain.notification.application.listener;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class SaleCreatedEventListenerIntegrationTest {

    @Autowired
    private SaleCreatedEventListener listener;

    @Test
    public void testSendEmailReal() throws InterruptedException {
        System.out.println("--- STARTING REAL EMAIL SENDING TEST ---");
        
        MasterDataResponseDto payDto = new MasterDataResponseDto(1L, "EFE", "Efectivo");
        MasterDataResponseDto statusDto = new MasterDataResponseDto(2L, "ACT", "Activo");
        SaleItemResponse item = new SaleItemResponse(10L, "Producto Test", "2.0", new BigDecimal("1500.00"), new BigDecimal("3000.00"));
        
        SaleResponse saleResponse = new SaleResponse(
                100L,
                UUID.randomUUID(),
                new BigDecimal("3000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                LocalDateTime.now(),
                payDto,
                statusDto,
                "Cliente De Prueba",
                "10203040",
                4L,
                1L,
                List.of(item)
        );

        SaleCreatedEvent event = new SaleCreatedEvent(this, saleResponse, "friastto@gmail.com");
        
        try {
            listener.handleSaleCreated(event);
            System.out.println("--- REAL EMAIL SENDING TEST INVOKED, WAITING FOR ASYNC THREAD ---");
            Thread.sleep(15000); // Esperar 15 segundos para dar tiempo a la tarea asíncrona
            System.out.println("--- WAITING FINISHED ---");
        } catch (Exception e) {
            System.err.println("--- TEST FAILED WITH EXCEPTION ---");
            e.printStackTrace();
        }
    }
}
