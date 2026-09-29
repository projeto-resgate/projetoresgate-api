package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.AddNaturalPersonToFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddNaturalPersonToFamilyGroupService implements AddNaturalPersonToFamilyGroupUseCase {

    private final FamilyGroupRepository familyGroupRepository;
    private final NaturalPersonRepository naturalPersonRepository;

    public AddNaturalPersonToFamilyGroupService(FamilyGroupRepository familyGroupRepository,
                                                NaturalPersonRepository naturalPersonRepository) {
        this.familyGroupRepository = familyGroupRepository;
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional
    public void handle(AddNaturalPersonToFamilyGroupCommand command) {
        familyGroupRepository.findByIdOrThrow(command.familyGroupId());
        naturalPersonRepository.findByIdOrThrow(command.naturalPersonId());

        if (familyGroupRepository.linkNaturalPerson(command.familyGroupId(), command.naturalPersonId()) == 0) {
            throw new IllegalStateException("Esta pessoa já está vinculada a este grupo familiar.");
        }
    }
}
