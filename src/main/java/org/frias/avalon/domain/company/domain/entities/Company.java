package org.frias.avalon.domain.company.domain.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.outlet.entities.Outlet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String nit;

    private String address;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_company_status"))
    private MasterData status;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Outlet> outlets = new ArrayList<>();

    public Company(Long id){ this.id = id; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public void addSucursal(Outlet s) {
        s.setEmpresaId(this.id);
        s.setCompany(this);

        outlets.add(s);
    }


}
