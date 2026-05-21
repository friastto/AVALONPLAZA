package org.frias.avalon.domain.masterdata.infraestructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_data",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "short_name"),
                @UniqueConstraint(columnNames = "full_name")

        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortName;

    private String fullName;

    private Long parentId; // "parent" es el estándar para dependencias jerárquicas

    private Long statusId;

    // este constructor extra lo usamos para mapear DTOs sin tener que traer toda la entidad
    public MasterData(Long id) {
        this.id = id;
    }

}
