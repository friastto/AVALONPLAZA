package org.frias.avalon.domain.order.application.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "El productOutletId es obligatorio")
    private Long productOutletId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal unitPrice;

    @JsonSetter("quantity")
    public void setQuantityFromJson(Object qty) {
        if (qty == null) {
            this.quantity = null;
        } else if (qty instanceof Number num) {
            this.quantity = BigDecimal.valueOf(num.doubleValue());
        } else {
            String str = qty.toString().trim().replace(",", ".");
            this.quantity = str.isEmpty() ? null : new BigDecimal(str);
        }
    }

    public void setQuantity(Integer qty) {
        this.quantity = qty != null ? BigDecimal.valueOf(qty) : null;
    }

    public void setQuantity(BigDecimal qty) {
        this.quantity = qty;
    }
}
