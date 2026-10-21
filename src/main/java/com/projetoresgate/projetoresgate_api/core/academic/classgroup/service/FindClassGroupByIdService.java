package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindClassGroupByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindClassGroupByIdService implements FindClassGroupByIdUseCase {

    private final ClassGroupRepository repository;
    private final ProgramRepository programRepository;
    private final LegalPersonRepository legalPersonRepository;

    public FindClassGroupByIdService(ClassGroupRepository repository,
                                     ProgramRepository programRepository,
                                     LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.programRepository = programRepository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassGroupResponse handle(FindClassGroupByIdQuery query) {
        ClassGroup classGroup = repository.findByIdOrThrow(query.id());

        String programName = programRepository.findById(classGroup.getProgramId())
                .map(Program::getName)
                .orElse(null);
        String institutionName = legalPersonRepository.findById(classGroup.getInstitutionId())
                .map(LegalPerson::getDisplayName)
                .orElse(null);

        return ClassGroupResponse.fromEntity(classGroup, programName, institutionName);
    }
}
