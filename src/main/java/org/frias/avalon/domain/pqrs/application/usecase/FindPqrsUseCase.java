package org.frias.avalon.domain.pqrs.application.usecase;

import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface FindPqrsUseCase {
    Page<PqrsResponse> findAll(String statusCode, String typeCode, String search, Pageable pageable);
    PqrsResponse findById(Long id);
    Map<String, Object> getPqrsStats();
}
