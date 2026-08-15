package org.frias.avalon.domain.product.infraestructure.specification;

import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    /**
     * Devuelve una Specification para filtrar productos por nombre.
     * La búsqueda no distingue mayúsculas de minúsculas y busca coincidencias parciales.
     *
     * @param name El nombre o parte del nombre a buscar.
     * @return Una {@link Specification} para usar con JPA.
     */
    public static Specification<ProductOutlet> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Devuelve una condición que siempre es verdadera si el nombre es nulo/vacío
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("localName")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Devuelve una Specification para filtrar productos por el ID de la tienda (outlet).
     *
     * @param outletId El ID de la tienda por la que filtrar.
     * @return Una {@link Specification} para usar con JPA.
     */
    public static Specification<ProductOutlet> hasOutletId(Long outletId) {
        return (root, query, criteriaBuilder) -> {
            if (outletId == null) {
                return criteriaBuilder.conjunction(); // No filtra si el ID es nulo
            }
            return criteriaBuilder.equal(root.get("outletId"), outletId);
        };
    }

    /**
     * Devuelve una Specification para filtrar productos por el ID de categoria.
     *
     * @param categoryId El ID de la categoria por la que filtrar.
     * @return Una {@link Specification} para usar con JPA.
     */
    public static Specification<ProductOutlet> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            // Si el producto en tienda tiene vinculacion corporativa, filtrar a traves de productCompany
            if (root.get("productCompany") != null) {
                return criteriaBuilder.equal(root.join("productCompany", jakarta.persistence.criteria.JoinType.LEFT).get("productId"), categoryId);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
