package org.frias.avalon.domain.outlet.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.core.tenant.config.TenantEntity;
import org.frias.avalon.core.tenant.config.TenantEntityListener;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "outlets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code_outlet", "company_id"})
        })
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :empresaId")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Outlet extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    private String name;

    private String address;

    private String phone;

    @Column(nullable = false)
    private boolean isMain = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_outlet_status"))
    private MasterData status;


    private  Double latitude;


    private Double longitude;



    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            foreignKey = @ForeignKey(name = "fk_outlet_company")
    )
    private Company company;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void codeGenerator() {
        if (this.code == null || this.code.isBlank()) {
            this.code = generateCode();
        }
    }

    private String generateCode() {
        // Podrías usar un UUID corto o una lógica más personalizada
        return "Oult-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
