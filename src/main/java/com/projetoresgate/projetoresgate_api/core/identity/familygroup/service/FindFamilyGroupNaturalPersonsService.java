package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupNaturalPersonsUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindFamilyGroupNaturalPersonsService implements FindFamilyGroupNaturalPersonsUseCase {

    private final FamilyGroupRepository repository;

    public FindFamilyGroupNaturalPersonsService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NaturalPerson> handle(FindFamilyGroupNaturalPersonsQuery query) {
        FamilyGroup familyGroup = repository.findByIdOrThrow(query.familyGroupId());

        return List.copyOf(familyGroup.getNaturalPersonList());
    }
}
