package org.frias.avalon.domain.roleassignment.infraestructure;

import org.frias.avalon.domain.roleassignment.domain.entity.ScheduleDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDetailRepository extends JpaRepository<ScheduleDetail,Long> {
}
