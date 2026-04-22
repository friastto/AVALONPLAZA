package org.frias.avalon.domain.roleassignment.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.outlet.entities.Outlet;

import java.util.Date;
import java.util.List;

@Table
@Entity(name = "schedule")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id")
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private MasterData status;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleDetail> details;

}
