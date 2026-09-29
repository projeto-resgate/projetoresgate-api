package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class FindFamilyGroupNaturalPersonsService implements FindFamilyGroupNaturalPersonsUseCase {

    private final FamilyGroupRepository repository;

    public FindFamilyGroupNaturalPersonsService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FamilyGroupNaturalPersonResponse> handle(FindFamilyGroupNaturalPersonsQuery query) {
        repository.findByIdOrThrow(query.familyGroupId());

        Page<NaturalPerson> page = repository.findNaturalPersonsByFamilyGroupId(
                query.familyGroupId(), query.pageable());

        return new PageImpl<>(
                toResponses(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<FamilyGroupNaturalPersonResponse> toResponses(List<NaturalPerson> naturalPersons) {
        if (naturalPersons.isEmpty()) {
            return Collections.emptyList();
        }

        return naturalPersons.stream().map(FamilyGroupNaturalPersonResponse::fromEntity).toList();
    }
}
