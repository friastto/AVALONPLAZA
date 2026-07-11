package org.frias.avalon.domain.notification.domain.service;

import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;

public interface TicketGeneratorService {
    byte[] generateTicketPdf(SaleResponse sale);
}