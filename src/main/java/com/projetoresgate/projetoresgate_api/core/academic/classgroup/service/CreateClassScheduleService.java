package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassScheduleUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateClassScheduleService implements CreateClassScheduleUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassScheduleRepository scheduleRepository;
    private final ProgramRepository programRepository;

    public CreateClassScheduleService(ClassGroupRepository classGroupRepository,
                                      ClassScheduleRepository scheduleRepository,
                                      ProgramRepository programRepository) {
        this.classGroupRepository = classGroupRepository;
        this.scheduleRepository = scheduleRepository;
        this.programRepository = programRepository;
    }

    @Override
    @Transactional
    public ClassScheduleResponse handle(CreateClassScheduleCommand command) {
        ClassGroup classGroup = classGroupRepository.findByIdOrThrow(command.classGroupId());

        ClassSchedule schedule = ClassSchedule.create(
                classGroup, command.weekDay(), command.startTime(), command.endTime());
        ClassSchedule saved = scheduleRepository.save(schedule);

        refreshTitle(classGroup);

        return ClassScheduleResponse.fromEntity(saved);
    }

    private void refreshTitle(ClassGroup classGroup) {
        Program program = programRepository.findByIdOrThrow(classGroup.getProgramId());
        List<WeekDay> weekDays = scheduleRepository.findActiveWeekDaysByClassGroupId(classGroup.getId());
        classGroup.refreshTitle(program.getName(), weekDays);
    }
}
