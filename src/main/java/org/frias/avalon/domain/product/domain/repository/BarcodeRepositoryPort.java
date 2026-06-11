package org.frias.avalon.domain.product.domain.repository;

import org.frias.avalon.domain.product.domain.BarcodeDomain;
import java.util.Optional;

public interface BarcodeRepositoryPort {
    /**
     * Guarda un nuevo código de barras en la persistencia.
     *
     * @param barcodeDomain El objeto de dominio del código de barras a guardar.
     * @return El código de barras guardado, posiblemente con un ID actualizado.
     */
    BarcodeDomain save(BarcodeDomain barcodeDomain);

    /**
     * Busca un código de barras por su código.
     *
     * @param code El código de barras a buscar.
     * @return Un Optional que contiene el BarcodeDomain si se encuentra, o un Optional vacío si no.
     */
    Optional<BarcodeDomain> findByCode(String code);
}