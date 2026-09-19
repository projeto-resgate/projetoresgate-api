package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.FindProgramByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class FindProgramByIdService implements FindProgramByIdUseCase {

    private final ProgramRepository repository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public FindProgramByIdService(ProgramRepository repository, FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    public ProgramResponse handle(FindProgramByIdQuery query) {
        return toResponse(repository.findByIdOrThrow(query.id()));
    }

    private ProgramResponse toResponse(Program program) {
        LegalPersonSummaryResponse institution = resolveInstitution(program.getInstitutionId());
        return ProgramResponse.fromEntity(program, institution);
    }

    private LegalPersonSummaryResponse resolveInstitution(UUID institutionId) {
        if (institutionId == null) {
            return null;
        }
        return findLegalPersonSummariesUseCase.handle(Set.of(institutionId))
                .stream()
                .findFirst()
                .orElse(null);
    }
}