package org.frias.avalon.domain.pqrs.infrastructure.persistence.specification;

import org.frias.avalon.domain.pqrs.infrastructure.persistence.entity.PqrsEntity;
import org.springframework.data.jpa.domain.Specification;

public class PqrsSpecification {

    public static Specification<PqrsEntity> hasStatusCode(String statusCode) {
        return (root, query, cb) -> {
            if (statusCode == null || statusCode.isBlank() || "ALL".equalsIgnoreCase(statusCode.trim())) {
                return cb.conjunction();
            }
            return cb.equal(cb.upper(root.get("statusCode")), statusCode.trim().toUpperCase());
        };
    }

    public static Specification<PqrsEntity> hasTypeCode(String typeCode) {
        return (root, query, cb) -> {
            if (typeCode == null || typeCode.isBlank() || "ALL".equalsIgnoreCase(typeCode.trim())) {
                return cb.conjunction();
            }
            return cb.equal(cb.upper(root.get("typeCode")), typeCode.trim().toUpperCase());
        };
    }

    public static Specification<PqrsEntity> hasSearchText(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("ticketNumber")), pattern),
                    cb.like(cb.lower(root.get("subject")), pattern),
                    cb.like(cb.lower(root.get("contactEmail")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
