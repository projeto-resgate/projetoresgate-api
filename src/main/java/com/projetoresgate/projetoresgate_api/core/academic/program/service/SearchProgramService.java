package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.SearchProgramUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.SearchProgramQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchProgramService implements SearchProgramUseCase {

    private final ProgramRepository repository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public SearchProgramService(ProgramRepository repository, FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    public Page<ProgramResponse> handle(SearchProgramQuery query) {

        Specification<Program> genericFilters = new SpecificationBuilder<Program>()
                .with("status", ":", query.status())
                .with("institutionId", ":", query.institutionId())
                .build();

        Page<Program> page = repository.findAll(genericFilters, query.pageable());

        Map<UUID, LegalPersonSummaryResponse> institutionMap = findInstitutionSummaries(
                page.getContent().stream().map(Program::getInstitutionId).toList());

        return page.map(program -> ProgramResponse.fromEntity(program,
                program.getInstitutionId() == null ? null : institutionMap.get(program.getInstitutionId())));
    }

    private Map<UUID, LegalPersonSummaryResponse> findInstitutionSummaries(List<UUID> institutionIds) {
        Set<UUID> ids = institutionIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return findLegalPersonSummariesUseCase.handle(ids).stream()
                .collect(Collectors.toMap(LegalPersonSummaryResponse::id, Function.identity()));
    }
}