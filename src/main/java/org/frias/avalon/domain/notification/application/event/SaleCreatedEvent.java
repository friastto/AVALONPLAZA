package org.frias.avalon.domain.notification.application.event;

import lombok.Getter;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.springframework.context.ApplicationEvent;

@Getter
public class SaleCreatedEvent extends ApplicationEvent {
    private final SaleResponse saleResponse;
    private final String clientEmail;

    public SaleCreatedEvent(Object source, SaleResponse saleResponse, String clientEmail) {
        super(source);
        this.saleResponse = saleResponse;
        this.clientEmail = clientEmail;
    }
}