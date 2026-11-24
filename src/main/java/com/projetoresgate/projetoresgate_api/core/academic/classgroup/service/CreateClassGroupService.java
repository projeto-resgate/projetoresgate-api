package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.SchoolGradeRef;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.SchoolGradeRefRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
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

@Service
public class CreateClassGroupService implements CreateClassGroupUseCase {

    private final ClassGroupRepository repository;
    private final ProgramRepository programRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final SchoolGradeRepository schoolGradeRepository;
    private final SchoolGradeRefRepository schoolGradeRefRepository;

    public CreateClassGroupService(ClassGroupRepository repository,
                                   ProgramRepository programRepository,
                                   LegalPersonRepository legalPersonRepository,
                                   SchoolGradeRepository schoolGradeRepository,
                                   SchoolGradeRefRepository schoolGradeRefRepository) {
        this.repository = repository;
        this.programRepository = programRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.schoolGradeRepository = schoolGradeRepository;
        this.schoolGradeRefRepository = schoolGradeRefRepository;
    }

    @Override
    @Transactional
    public ClassGroupResponse handle(CreateClassGroupCommand command) {
        Program program = programRepository.findByIdOrThrow(command.programId());
        LegalPerson institution = legalPersonRepository.findByIdOrThrow(command.institutionId());

        List<SchoolGradeRef> schoolGrades = loadSchoolGrades(command.schoolGradeIds());

        ClassGroup classGroup = ClassGroup.create(
                command.programId(),
                command.institutionId(),
                command.studentProjections(),
                command.startDate(),
                command.durationInMonths(),
                command.note(),
                program.getName(),
                schoolGrades
        );

        ClassGroup saved = repository.save(classGroup);

        return ClassGroupResponse.fromEntity(saved, program.getName(), institution.getDisplayName());
    }

    private List<SchoolGradeRef> loadSchoolGrades(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<UUID> distinctIds = new LinkedHashSet<>(ids);

        if (schoolGradeRepository.findAllById(distinctIds).size() != distinctIds.size()) {
            throw new InternalException("Alguma série escolar informada não foi encontrada.");
        }

        return new ArrayList<>(schoolGradeRefRepository.findAllById(distinctIds));
    }
}
