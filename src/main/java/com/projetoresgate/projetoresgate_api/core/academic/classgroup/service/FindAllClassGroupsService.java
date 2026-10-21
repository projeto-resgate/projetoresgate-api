package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassGroupsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class FindAllClassGroupsService implements FindAllClassGroupsUseCase {

    private final ClassGroupRepository repository;
    private final ProgramRepository programRepository;
    private final FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    public FindAllClassGroupsService(ClassGroupRepository repository,
                                     ProgramRepository programRepository,
                                     FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase) {
        this.repository = repository;
        this.programRepository = programRepository;
        this.findLegalPersonSummariesUseCase = findLegalPersonSummariesUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassGroupSummaryResponse> handle(FindAllClassGroupsQuery query) {

        Specification<ClassGroup> filters = new SpecificationBuilder<ClassGroup>()
                .with("title", "~", query.searchTerm())
                .with("programId", ":", query.programId())
                .with("institutionId", ":", query.institutionId())
                .with("active", ":", query.active())
                .build();

        Page<ClassGroup> page = repository.findAll(filters, query.pageable());

        Map<UUID, String> programNames = resolveProgramNames(page.getContent());
        Map<UUID, String> institutionNames = resolveInstitutionNames(page.getContent());

        List<ClassGroupSummaryResponse> content = page.getContent().stream()
                .map(classGroup -> ClassGroupSummaryResponse.fromEntity(
                        classGroup,
                        isNull(classGroup.getProgramId()) ? null : programNames.get(classGroup.getProgramId()),
                        isNull(classGroup.getInstitutionId()) ? null : institutionNames.get(classGroup.getInstitutionId())))
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
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

    private Map<UUID, String> resolveInstitutionNames(List<ClassGroup> classGroups) {
        Set<UUID> ids = classGroups.stream()
                .map(ClassGroup::getInstitutionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return findLegalPersonSummariesUseCase.handle(ids).stream()
                .collect(Collectors.toMap(LegalPersonSummaryResponse::id, LegalPersonSummaryResponse::displayName));
    }
}
