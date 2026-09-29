package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindAllFamilyGroupsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindAllFamilyGroupsQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FindAllFamilyGroupsService implements FindAllFamilyGroupsUseCase {

    private final FamilyGroupRepository repository;

    public FindAllFamilyGroupsService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FamilyGroupSummaryResponse> handle(FindAllFamilyGroupsQuery query) {

        Specification<FamilyGroup> filters = new SpecificationBuilder<FamilyGroup>()
                .with("name", "~", query.name())
                .build();

        Page<FamilyGroup> page = repository.findAll(filters, query.pageable());

        return new PageImpl<>(
                toSummaries(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<FamilyGroupSummaryResponse> toSummaries(List<FamilyGroup> familyGroups) {
        if (familyGroups.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Long> countsById = new HashMap<>();
        repository.countRegisteredPeopleByIds(familyGroups.stream().map(FamilyGroup::getId).toList())
                .forEach(projection -> countsById.put(projection.getFamilyGroupId(),
                        projection.getRegisteredPeopleCount()));

        return familyGroups.stream()
                .map(familyGroup -> FamilyGroupSummaryResponse.fromEntity(familyGroup,
                        countsById.getOrDefault(familyGroup.getId(), 0L)))
                .toList();
    }
}
