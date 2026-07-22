package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request para procesar una devolución/cambio en POS.
 *
 * @param originalSaleCode  UUID del ticket/factura original presentado por el cliente
 * @param reason            Motivo: DEFECTO | INCORRECTO | OTRO
 * @param resolutionType    Resolución: REEMBOLSO | NOTA_CREDITO | CAMBIO
 * @param items             Productos a devolver con sus cantidades
 * @param sendEmail         Si se envía comprobante por correo al cliente
 */
public record CreateReturnRequest(
        @NotNull(message = "El código de la venta original es requerido")
        UUID originalSaleCode,

        @NotBlank(message = "El motivo de devolución es requerido")
        String reason,

        String notes,

        @NotBlank(message = "El tipo de resolución es requerido")
        String resolutionType,

        @NotEmpty(message = "Debe incluir al menos un producto a devolver")
        List<ReturnItemRequest> items,

        Boolean sendEmail
) {}
