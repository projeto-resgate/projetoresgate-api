package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.FindFamilyGroupByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupByIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindFamilyGroupByIdService implements FindFamilyGroupByIdUseCase {

    private final FamilyGroupRepository repository;

    public FindFamilyGroupByIdService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public FamilyGroup handle(FindFamilyGroupByIdQuery query) {
        return repository.findByIdOrThrow(query.id());
    }
}
