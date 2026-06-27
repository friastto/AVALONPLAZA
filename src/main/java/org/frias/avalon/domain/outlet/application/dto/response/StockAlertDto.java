package org.frias.avalon.domain.outlet.application.dto.response;

public record StockAlertDto(
        String productName,
        int currentStock,
        int minStock,
        String alertType // "MINIMUM", "EXPIRY_WARNING", "EXPIRED"
) {}
