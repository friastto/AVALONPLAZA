package org.frias.avalon.core.tenant.config;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

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
