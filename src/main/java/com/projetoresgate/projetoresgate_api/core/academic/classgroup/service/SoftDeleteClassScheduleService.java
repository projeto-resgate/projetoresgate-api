package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SoftDeleteClassScheduleService implements SoftDeleteClassScheduleUseCase {

    private final ClassScheduleRepository repository;
    private final ProgramRepository programRepository;

    public SoftDeleteClassScheduleService(ClassScheduleRepository repository,
                                          ProgramRepository programRepository) {
        this.repository = repository;
        this.programRepository = programRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteClassScheduleCommand command) {
        ClassSchedule schedule = repository.findByIdAndClassGroupIdOrThrow(command.id(), command.classGroupId());

        repository.delete(schedule);

        refreshTitle(schedule.getClassGroup());
    }

    private void refreshTitle(ClassGroup classGroup) {
        Program program = programRepository.findByIdOrThrow(classGroup.getProgramId());
        List<WeekDay> weekDays = repository.findActiveWeekDaysByClassGroupId(classGroup.getId());
        classGroup.refreshTitle(program.getName(), weekDays);
    }
}
