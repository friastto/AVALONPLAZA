package org.frias.avalon.ventas.dtos;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.ventas.entities.Sale;

import java.math.BigDecimal;


public record SaleDetailRequest (
    String quantity,

    BigDecimal unitPrice,

    Long productId
){
}
