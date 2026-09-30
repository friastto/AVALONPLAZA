package org.frias.avalon.domain.product.application.port;

import org.frias.avalon.domain.product.domain.ProductDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Output port for product persistence operations.
 */
public interface ProductOutletRepositoryPort {

    /**
     * Saves a product domain model.
     * @param productDomain The product to save.
     * @return The saved product domain model, typically with a generated ID.
     */
    ProductDomain save(ProductDomain productDomain);

    /**
     * Finds a product by its ID.
     * @param id The ID of the product.
     * @return An Optional containing the product if found.
     */
    Optional<ProductDomain> findById(Long id);

    /**
     * Retrieves a paginated and filtered list of products.
     * @param name A string to filter products by name (can be null).
     * @param outletId A Long to filter products by outlet ID (can be null).
     * @param pageable Pagination and sorting information.
     * @return A Page of product domain models.
     */
    Page<ProductDomain> findAll(String name, Long outletId, Pageable pageable);

    /**
     * Retrieves a paginated and filtered list of products.
     * @param name A string to filter products by name (can be null).
     * @param outletId A Long to filter products by outlet ID (can be null).
     * @param categoryId A Long to filter products by category ID (can be null).
     * @param pageable Pagination and sorting information.
     * @return A Page of product domain models.
     */
    Page<ProductDomain> findAll(String name, Long outletId, Long categoryId, Pageable pageable);

    /**
     * Retrieves products filtered by name and outlet ID that have stock greater than zero.
     * @param name Name filter (case-insensitive substring, can be null or empty).
     * @param outletId Outlet ID (can be null).
     * @param pageable Pagination and sorting information.
     * @return A Page of product domain models with stock > 0.
     */
    Page<ProductDomain> findAvailableByName(String name, Long outletId, Pageable pageable);

    /**
     * Recupera productos disponibles filtrados por nombre a traves de multiples tiendas
     * optimizando el acceso en una sola conexion JDBC.
     * @param name Nombre o fragmento a buscar.
     * @param outletIds Lista de identificadores de tiendas a consultar.
     * @param limitPerOutlet Maximo de productos por tienda.
     * @return Mapa de outletId a la lista de productos encontrados.
     */
    Map<Long, List<ProductDomain>> findAvailableByNameAcrossOutlets(String name, List<Long> outletIds, int limitPerOutlet);
}
