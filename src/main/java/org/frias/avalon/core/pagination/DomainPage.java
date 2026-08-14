package org.frias.avalon.core.pagination;

import java.util.Collections;
import java.util.List;

/**
 * Agnostic Domain Paging abstraction for decoupling Spring Data Page/Pageable from Domain Ports.
 *
 * @param <T> content element type
 */
public record DomainPage<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public DomainPage {
        if (content == null) {
            content = Collections.emptyList();
        }
    }

    public static <T> DomainPage<T> empty() {
        return new DomainPage<>(Collections.emptyList(), 0, 10, 0L, 0);
    }
}
