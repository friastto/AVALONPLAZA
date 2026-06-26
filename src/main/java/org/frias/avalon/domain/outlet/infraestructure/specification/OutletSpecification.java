package org.frias.avalon.domain.outlet.infraestructure.specification;

import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.springframework.data.jpa.domain.Specification;

public class OutletSpecification {

    public static Specification<Outlet> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Outlet> hasNit(String nit) {
        return (root, query, criteriaBuilder) -> {
            if (nit == null || nit.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("nit"), nit.trim());
        };
    }

    public static Specification<Outlet> hasCode(String code) {
        return (root, query, criteriaBuilder) -> {
            if (code == null || code.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("code")), code.trim().toLowerCase());
        };
    }

    public static Specification<Outlet> hasAddress(String address) {
        return (root, query, criteriaBuilder) -> {
            if (address == null || address.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%");
        };
    }

    public static Specification<Outlet> hasStatusId(Long statusId) {
        return (root, query, criteriaBuilder) -> {
            if (statusId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("statusId"), statusId);
        };
    }
}
