package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.UpdateClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class UpdateClassScheduleService implements UpdateClassScheduleUseCase {

    private final ClassScheduleRepository repository;
    private final ProgramRepository programRepository;

    public UpdateClassScheduleService(ClassScheduleRepository repository,
                                      ProgramRepository programRepository) {
        this.repository = repository;
        this.programRepository = programRepository;
    }

    @Override
    @Transactional
    public ClassScheduleResponse handle(UpdateClassScheduleCommand command) {
        ClassSchedule schedule = repository.findByIdAndClassGroupIdOrThrow(command.id(), command.classGroupId());

        schedule.update()
                .weekDay(command.weekDay())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .apply();

        if (nonNull(command.active())) {
            if (command.active()) {
                schedule.activate();
            } else {
                schedule.deactivate();
            }
        }

        ClassSchedule saved = repository.save(schedule);

        refreshTitle(saved.getClassGroup());

        return ClassScheduleResponse.fromEntity(saved);
    }

    private void refreshTitle(ClassGroup classGroup) {
        Program program = programRepository.findByIdOrThrow(classGroup.getProgramId());
        List<WeekDay> weekDays = repository.findActiveWeekDaysByClassGroupId(classGroup.getId());
        classGroup.refreshTitle(program.getName(), weekDays);
    }
}
