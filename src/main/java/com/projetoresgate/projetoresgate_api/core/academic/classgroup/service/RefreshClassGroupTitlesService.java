package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.RefreshClassGroupTitlesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.RefreshClassGroupTitlesCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class RefreshClassGroupTitlesService implements RefreshClassGroupTitlesUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassScheduleRepository scheduleRepository;
    private final ProgramRepository programRepository;

    public RefreshClassGroupTitlesService(ClassGroupRepository classGroupRepository,
                                          ClassScheduleRepository scheduleRepository,
                                          ProgramRepository programRepository) {
        this.classGroupRepository = classGroupRepository;
        this.scheduleRepository = scheduleRepository;
        this.programRepository = programRepository;
    }

    @Override
    @Transactional
    public void handle(RefreshClassGroupTitlesCommand command) {
        List<ClassGroup> classGroups = resolveClassGroups(command);
        if (classGroups.isEmpty()) {
            return;
        }

        Map<UUID, String> programNames = resolveProgramNames(classGroups);
        Map<UUID, List<WeekDay>> weekDays = resolveWeekDays(classGroups);

        for (ClassGroup classGroup : classGroups) {
            String programName = programNames.get(classGroup.getProgramId());
            if (!StringUtils.hasText(programName)) {
                throw new InternalException("Não foi possível gerar o título da turma.");
            }
            classGroup.refreshTitle(programName, weekDays.getOrDefault(classGroup.getId(), List.of()));
        }

        classGroupRepository.saveAll(classGroups);
    }

    private List<ClassGroup> resolveClassGroups(RefreshClassGroupTitlesCommand command) {
        if (nonNull(command.programId())) {
            return classGroupRepository.findByProgramId(command.programId());
        }
        if (nonNull(command.schoolGradeId())) {
            return classGroupRepository.findBySchoolGradeId(command.schoolGradeId());
        }
        return Collections.emptyList();
    }

    private Map<UUID, String> resolveProgramNames(List<ClassGroup> classGroups) {
        Set<UUID> ids = classGroups.stream()
                .map(ClassGroup::getProgramId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return programRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Program::getId, Program::getName));
    }

    private Map<UUID, List<WeekDay>> resolveWeekDays(List<ClassGroup> classGroups) {
        List<UUID> ids = classGroups.stream()
                .map(ClassGroup::getId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return scheduleRepository.findActiveWeekDaysByClassGroupIds(ids).stream()
                .collect(Collectors.groupingBy(
                        row -> (UUID) row[0],
                        Collectors.mapping(row -> (WeekDay) row[1], Collectors.toCollection(ArrayList::new))));
    }
}
