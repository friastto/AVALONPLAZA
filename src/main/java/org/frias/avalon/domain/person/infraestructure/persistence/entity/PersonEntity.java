package org.frias.avalon.domain.person.infraestructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "person", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"identification_id", "number_id"})
})

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numberId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    private String address;

    @Column(nullable = false)
    private Long identificationId; // FK a MasterData


    private Long sexId; // FK a MasterData


    private Long phoneNumber;

    private String email;

    @Column(nullable = false)
    private Long statusId; // FK a MasterData

    @Column(nullable = false, updatable = false)
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
}