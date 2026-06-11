package org.frias.avalon.domain.product.infraestructure.mapper;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;

/**
 * Port for mapping between Product domain, entity, and DTOs.
 */
public interface ProductOutletMapper {

    /**
     * Maps a ProductOutlet entity to a ProductDomain model.
     * @param entity The JPA entity.
     * @return The domain model.
     */
    ProductDomain toDomain(ProductOutlet entity);

    /**
     * Maps a ProductDomain model to a ProductOutlet entity.
     * @param domain The domain model.
     * @return The JPA entity.
     */
    ProductOutlet toEntity(ProductDomain domain);

    /**
     * Maps a ProductDomain model to a ProductResponse DTO.
     * This may require external data to enrich the DTO (e.g., status name).
     * @param domain The domain model.
     * @return The response DTO.
     */
    ProductResponse toResponse(ProductDomain domain);
}
