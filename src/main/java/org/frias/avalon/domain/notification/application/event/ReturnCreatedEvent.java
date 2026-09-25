package org.frias.avalon.domain.notification.application.event;

import lombok.Getter;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReturnCreatedEvent extends ApplicationEvent {
    private final ReturnResponse returnResponse;
    private final String clientEmail;

    public ReturnCreatedEvent(Object source, ReturnResponse returnResponse, String clientEmail) {
        super(source);
        this.returnResponse = returnResponse;
        this.clientEmail = clientEmail;
    }
}
