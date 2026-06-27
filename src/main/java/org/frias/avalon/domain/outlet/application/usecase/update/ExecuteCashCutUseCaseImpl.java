package org.frias.avalon.domain.outlet.application.usecase.update;

import org.frias.avalon.domain.outlet.application.dto.response.CashCutResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación del Caso de Uso para la ejecución del arqueo/corte final de caja de la tienda.
 */
@Service
public class ExecuteCashCutUseCaseImpl implements ExecuteCashCutUseCase {

    @Override
    public CashCutResponse execute(Long outletId) {
        return new CashCutResponse(
                true,
                "Cierre de caja y arqueo general ejecutado exitosamente para la tienda " + outletId,
                new BigDecimal("2100000"), // total de caja cerrado
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
