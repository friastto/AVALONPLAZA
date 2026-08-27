package org.frias.avalon.domain.notification.application.listener;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@Transactional
public class SaleCreatedEventListenerIntegrationTest {

    @Autowired
    private SaleCreatedEventListener listener;

    @MockitoBean
    private EmailSenderPort emailSenderPort;

    @Test
    @DisplayName("Should handle sale created event and invoke email sender port")
    public void testSendEmailReal() {
        System.out.println("--- STARTING SALE CREATED EVENT LISTENER INTEGRATION TEST ---");
        
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

        SaleCreatedEvent event = new SaleCreatedEvent(this, saleResponse, "test@avalon.com");
        
        listener.handleSaleCreated(event);

        verify(emailSenderPort).sendEmailWithAttachment(
                eq("test@avalon.com"),
                any(String.class),
                any(String.class),
                any(byte[].class),
                any(String.class)
        );
        System.out.println("--- SALE CREATED EVENT LISTENER TEST FINISHED ---");
    }
}
