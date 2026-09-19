package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.CreateProgramUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.CreateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CreateProgramService implements CreateProgramUseCase {

    private final ProgramRepository repository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public CreateProgramService(ProgramRepository repository, FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    @Transactional
    public ProgramResponse handle(CreateProgramCommand command) {
        if (repository.existsByName(command.name())) {
            throw new InternalException("Já existe um programa cadastrado com este nome.");
        }

        Program program = Program.create(
                command.name(),
                command.webSiteUrl(),
                command.status(),
                command.institutionId()
        );

        if (command.educatorCategoryItems() != null && !command.educatorCategoryItems().isEmpty()) {
            List<EducatorCategoryItem> items = command.educatorCategoryItems().stream()
                    .map(item -> EducatorCategoryItem.create(
                            item.name(),
                            program))
                    .toList();
            program.getEducatorCategoryItemList().addAll(items);
        }

        return toResponse(repository.save(program));
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