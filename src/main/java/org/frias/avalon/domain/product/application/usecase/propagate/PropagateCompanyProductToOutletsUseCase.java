package org.frias.avalon.domain.product.application.usecase.propagate;

public interface PropagateCompanyProductToOutletsUseCase {
    /**
     * Propaga un producto corporativo aprobado (Nivel 2) hacia todas las tiendas de la empresa (Nivel 3).
     *
     * @param productCompanyId ID del producto corporativo en public.product_company
     * @return Numero de tiendas a las que fue propagado el producto
     */
    int execute(Long productCompanyId);
}
