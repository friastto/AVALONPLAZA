package org.frias.avalon.domain.roleassignment.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.roleassignment.domain.entity.Schedule;
import org.frias.avalon.domain.roleassignment.infraestructure.ScheduleRepository;

public class ScheduleServiceImpl implements ScheduleService{

    private final ScheduleRepository scheduleRepository;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }


    @Override
    public Schedule searchById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(()->new EntityNotFoundException("no tiene horario definido"));
    }

    @Override
    public Schedule save(Schedule s) {


        return null;
    }


}
