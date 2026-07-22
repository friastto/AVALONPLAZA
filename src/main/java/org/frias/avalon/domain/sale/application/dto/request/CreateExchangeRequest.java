package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request para procesar un intercambio de productos (Devolución + Reemplazo).
 *
 * @param originalSaleCode  UUID del ticket de la venta original
 * @param reason            Motivo: DEFECTO | INCORRECTO | OTRO
 * @param returnedItems     Ítems que el cliente entrega para devolución
 * @param exchangeItems     Nuevos ítems que el cliente se lleva en reemplazo
 * @param paymentMethodId   Método de pago para el excedente (Efectivo, FIA, Nequi, etc.)
 * @param amountReceived    Monto entregado si paga el excedente en efectivo
 * @param sendEmail         Envío de comprobante por correo
 */
public record CreateExchangeRequest(
        @NotNull(message = "El código de la venta original es requerido")
        UUID originalSaleCode,

        @NotBlank(message = "El motivo de la devolución es requerido")
        String reason,

        String notes,

        @NotEmpty(message = "Debe incluir al menos un producto a devolver")
        List<ReturnItemRequest> returnedItems,

        @NotEmpty(message = "Debe incluir al menos un producto de reemplazo")
        List<ExchangeItemRequest> exchangeItems,

        @NotNull(message = "El método de pago para el excedente es requerido")
        Long paymentMethodId,

        BigDecimal amountReceived,

        Boolean sendEmail
) {}
