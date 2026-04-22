package org.frias.avalon.domain.roleassignment.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;

import java.time.LocalDateTime;

@Table
@Entity(name = "role_assignment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoleAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_avalon_id" , nullable = false)
    private UserAvalon userAvalon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private MasterData role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_scope_id",nullable = false)
    private MasterData staffScope;  //( AVALON, COMPANY, OUTLET, CONSUMER)

    @Column(name = "scope_id",nullable = false)
    private Long scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id",nullable = false)
    private Schedule schedule; // HORARIO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id",nullable = false)
    private MasterData status;

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
