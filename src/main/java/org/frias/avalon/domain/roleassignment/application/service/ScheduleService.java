package org.frias.avalon.domain.roleassignment.application.service;

import org.frias.avalon.domain.roleassignment.domain.entity.Schedule;

public interface ScheduleService {
    Schedule searchById(Long ScheduleId);
    Schedule save(Schedule s );
}
