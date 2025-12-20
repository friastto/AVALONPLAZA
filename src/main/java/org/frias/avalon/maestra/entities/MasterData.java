package org.frias.avalon.maestra.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_data",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "full_name"),
                @UniqueConstraint(columnNames = "short_name")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String shortName;

    private Long parentId; // "parent" es el estándar para dependencias jerárquicas

    private Long statusId;

    // este constructor extra lo usamos para mapear DTOs sin tener que traer toda la entidad
    public MasterData(Long id) {
        this.id = id;
    }

}
