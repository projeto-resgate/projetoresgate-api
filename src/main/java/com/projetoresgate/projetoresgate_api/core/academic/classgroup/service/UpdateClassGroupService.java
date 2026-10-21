package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.UpdateClassGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
public class UpdateClassGroupService implements UpdateClassGroupUseCase {

    private final ClassGroupRepository repository;
    private final ProgramRepository programRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final SchoolGradeRepository schoolGradeRepository;
    private final ClassScheduleRepository scheduleRepository;

    public UpdateClassGroupService(ClassGroupRepository repository,
                                   ProgramRepository programRepository,
                                   LegalPersonRepository legalPersonRepository,
                                   SchoolGradeRepository schoolGradeRepository,
                                   ClassScheduleRepository scheduleRepository) {
        this.repository = repository;
        this.programRepository = programRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.schoolGradeRepository = schoolGradeRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    @Transactional
    public ClassGroupResponse handle(UpdateClassGroupCommand command) {
        ClassGroup classGroup = repository.findByIdOrThrow(command.id());

        if (nonNull(command.programId())) {
            programRepository.findByIdOrThrow(command.programId());
        }
        if (nonNull(command.institutionId())) {
            legalPersonRepository.findByIdOrThrow(command.institutionId());
        }

        ClassGroup.Updater updater = classGroup.update()
                .programId(command.programId())
                .institutionId(command.institutionId())
                .studentProjections(command.studentProjections())
                .startDate(command.startDate())
                .durationInMonths(command.durationInMonths())
                .note(command.note())
                .active(command.active());

        if (nonNull(command.schoolGradeIds())) {
            updater.schoolGradeList(loadSchoolGrades(command.schoolGradeIds()));
        }

        updater.apply();

        refreshTitle(classGroup);

        ClassGroup saved = repository.save(classGroup);

        return ClassGroupResponse.fromEntity(saved, resolveProgramName(saved), resolveInstitutionName(saved));
    }

    private void refreshTitle(ClassGroup classGroup) {
        Program program = programRepository.findByIdOrThrow(classGroup.getProgramId());
        List<WeekDay> weekDays = scheduleRepository.findActiveWeekDaysByClassGroupId(classGroup.getId());
        classGroup.refreshTitle(program.getName(), weekDays);
    }

    private String resolveProgramName(ClassGroup classGroup) {
        return programRepository.findById(classGroup.getProgramId()).map(Program::getName).orElse(null);
    }

    private String resolveInstitutionName(ClassGroup classGroup) {
        return legalPersonRepository.findById(classGroup.getInstitutionId())
                .map(LegalPerson::getDisplayName)
                .orElse(null);
    }

    private List<SchoolGrade> loadSchoolGrades(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<UUID> distinctIds = new LinkedHashSet<>(ids);
        List<SchoolGrade> schoolGrades = schoolGradeRepository.findAllById(distinctIds);

        if (schoolGrades.size() != distinctIds.size()) {
            throw new InternalException("Alguma série escolar informada não foi encontrada.");
        }

        return new ArrayList<>(schoolGrades);
    }
}
