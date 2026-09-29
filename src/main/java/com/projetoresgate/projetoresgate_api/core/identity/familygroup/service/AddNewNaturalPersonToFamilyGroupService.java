package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.AddNewNaturalPersonToFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNewNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.CreateNaturalPersonUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddNewNaturalPersonToFamilyGroupService implements AddNewNaturalPersonToFamilyGroupUseCase {

    private final FamilyGroupRepository familyGroupRepository;
    private final CreateNaturalPersonUseCase createNaturalPersonUseCase;

    public AddNewNaturalPersonToFamilyGroupService(FamilyGroupRepository familyGroupRepository,
                                                   CreateNaturalPersonUseCase createNaturalPersonUseCase) {
        this.familyGroupRepository = familyGroupRepository;
        this.createNaturalPersonUseCase = createNaturalPersonUseCase;
    }

    @Override
    @Transactional
    public NaturalPerson handle(AddNewNaturalPersonToFamilyGroupCommand command) {
        familyGroupRepository.findByIdOrThrow(command.familyGroupId());

        NaturalPerson person = createNaturalPersonUseCase.handle(command.naturalPerson());

        familyGroupRepository.linkNaturalPerson(command.familyGroupId(), person.getId());

        return person;
    }
}
