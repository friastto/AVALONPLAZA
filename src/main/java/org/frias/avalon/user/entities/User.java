package org.frias.avalon.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.person.entity.Person;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_user_personsId"))
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "fk_user_statusId"))
    private MasterData statusId;

/*
    @ManyToOne
    @JoinColumn(name = "empr_id")
    private Empresa empresa;

     */
}

