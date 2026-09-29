package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SoftDeleteFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteFamilyGroupService implements SoftDeleteFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public SoftDeleteFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteFamilyGroupCommand command) {
        FamilyGroup familyGroup = repository.findByIdOrThrow(command.id());
        repository.delete(familyGroup);
    }
}
