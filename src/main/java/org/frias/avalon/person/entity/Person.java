package org.frias.avalon.person.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.maestra.entities.MasterData;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numberid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    private String dir;

    @ManyToOne(optional = false)
    @JoinColumn(name = "identification_id", foreignKey = @ForeignKey(name = "fk_person_masterdata_identificationId"))
    private MasterData identificationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sex_id", foreignKey = @ForeignKey(name = "fk_person_masterdata_sexId"))
    private MasterData sexId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "fk_person_masterdata_statusID"))
    private MasterData statusId;

    public Person(Long id) {
        this.id = id;
    }
}

