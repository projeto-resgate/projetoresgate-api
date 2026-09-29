package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNewNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

public interface AddNewNaturalPersonToFamilyGroupUseCase {
    NaturalPerson handle(AddNewNaturalPersonToFamilyGroupCommand command);
}
