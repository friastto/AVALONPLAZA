package org.frias.avalon.empresasucursal.tenant.config;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
    @Getter
@Setter
    public abstract class TenantEntity {

    @Column(
            name = "company_id",
            insertable = false,
            updatable = false
    )            // 🔥 ya NO mapea a BD
    private Long empresaId;  // solo para filtros / contexto

}
