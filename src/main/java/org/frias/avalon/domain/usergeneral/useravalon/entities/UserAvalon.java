package org.frias.avalon.domain.usergeneral.useravalon.entities;

import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.temp.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.temp.empresasucursal.tenant.config.TenantEntity;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.temp.person.entity.Person;

@Entity
@Table(name = "users_Avalon")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

public class UserAvalon extends TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @ManyToOne()
    @JoinColumn(name = "rol_id", foreignKey = @ForeignKey(name = "fk_user_rol"))
    private MasterData rolId;

    @Column(name = "hash_salt", nullable = false)
    private String hashSalt;

    @Column(name = "hash_password", nullable = false)
    private String hashPassword;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_user_personsId"))
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "fk_user_statusId"))
    private MasterData statusId;

    @ManyToOne
    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_user_companyId"))
    private Company companyId;

    @ManyToOne
    @JoinColumn(name = "outlet_id", foreignKey = @ForeignKey(name = "fk_user_outletId"))
    private Outlet outletId;

}

