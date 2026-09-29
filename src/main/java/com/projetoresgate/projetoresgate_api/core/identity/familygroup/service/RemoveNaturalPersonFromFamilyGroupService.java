package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.RemoveNaturalPersonFromFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.RemoveNaturalPersonFromFamilyGroupCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveNaturalPersonFromFamilyGroupService implements RemoveNaturalPersonFromFamilyGroupUseCase {

    private final FamilyGroupRepository familyGroupRepository;

    public RemoveNaturalPersonFromFamilyGroupService(FamilyGroupRepository familyGroupRepository) {
        this.familyGroupRepository = familyGroupRepository;
    }

    @Override
    @Transactional
    public void handle(RemoveNaturalPersonFromFamilyGroupCommand command) {
        familyGroupRepository.findByIdOrThrow(command.familyGroupId());

        if (familyGroupRepository.unlinkNaturalPerson(command.familyGroupId(), command.naturalPersonId()) == 0) {
            throw new IllegalStateException("Esta pessoa não está vinculada a este grupo familiar.");
        }
    }
}
