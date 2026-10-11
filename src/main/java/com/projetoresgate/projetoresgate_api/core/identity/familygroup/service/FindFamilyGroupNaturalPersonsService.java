package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindNaturalPersonRolesUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FindFamilyGroupNaturalPersonsService implements FindFamilyGroupNaturalPersonsUseCase {

    private final FamilyGroupRepository repository;
    private final FindNaturalPersonRolesUseCase findNaturalPersonRolesUseCase;

    public FindFamilyGroupNaturalPersonsService(FamilyGroupRepository repository,
                                                FindNaturalPersonRolesUseCase findNaturalPersonRolesUseCase) {
        this.repository = repository;
        this.findNaturalPersonRolesUseCase = findNaturalPersonRolesUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FamilyGroupNaturalPersonResponse> handle(FindFamilyGroupNaturalPersonsQuery query) {
        repository.findByIdOrThrow(query.familyGroupId());

        Page<NaturalPerson> page = repository.findNaturalPersonsByFamilyGroupId(
                query.familyGroupId(), query.pageable());

        Map<UUID, List<NaturalPersonRoleType>> rolesByNaturalPerson = findNaturalPersonRolesUseCase.handle(
                page.getContent().stream().map(NaturalPerson::getId).toList());

        return new PageImpl<>(
                toResponses(page.getContent(), rolesByNaturalPerson),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<FamilyGroupNaturalPersonResponse> toResponses(List<NaturalPerson> naturalPersons,
                                                               Map<UUID, List<NaturalPersonRoleType>> rolesByNaturalPerson) {
        if (naturalPersons.isEmpty()) {
            return Collections.emptyList();
        }

        return naturalPersons.stream()
                .map(naturalPerson -> FamilyGroupNaturalPersonResponse.fromEntity(
                        naturalPerson, rolesByNaturalPerson.getOrDefault(naturalPerson.getId(), List.of())))
                .toList();
    }
}