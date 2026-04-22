package org.frias.avalon.domain.roleassignment.infraestructure;

import org.frias.avalon.domain.roleassignment.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
}
